package com.example.Excel2Json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ExcelController {

	@PostMapping("/excel")
	public ExcelResponse getExcel(@RequestParam("local") MultipartFile local,@RequestParam("remote") MultipartFile remote, @RequestParam("name") String columnName) {
		return excel2Json(new MultipartFile[] {local,remote},columnName);
	}

	@GetMapping("/excel")
	public ExcelResponse getExcel(@RequestParam String uid) {
		return getExcelColumn(uid);
	}
	
	private ExcelResponse getExcelColumn(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

	public ExcelResponse excel2Json(MultipartFile[] data,String matchColumnName) {
		ExcelResponse response= new ExcelResponse();
		response.setId(UUID.randomUUID());
		List<JSONObject> dataList1 = new ArrayList<>();
		List<JSONObject> dataList2 = new ArrayList<>();

		for (int a = 0; a < data.length; a++) {
			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(data[a].getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			XSSFSheet workSheet = workbook.getSheetAt(0);
			XSSFRow header = workSheet.getRow(0);
			for (int i = 1; i < workSheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = workSheet.getRow(i);
				JSONObject rowJsonObject = new JSONObject();
				for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
					String columnName = header.getCell(j).toString();
					String columnValue = row.getCell(j).toString();
					rowJsonObject.put(columnName, columnValue);
				}
				if (a == 0) {
					dataList1.add(rowJsonObject);
				} else {
					dataList2.add(rowJsonObject);
				}

			}
		}
		System.out.println("first file : " + dataList1);
		System.out.println("second file : " + dataList2);

		List<JSONObject> result=new ArrayList<JSONObject>();
		List<JSONObject> matched=matchStream(dataList1, dataList2, matchColumnName);
		result.addAll(matched);
		
		List<JSONObject> removed=removedStream(dataList1, dataList2, matchColumnName);
		result.addAll(removed);
		
		List<JSONObject> added=addedStream(dataList1, dataList2, matchColumnName);
		result.addAll(added);
		
		matched.stream().distinct().forEach(System.err::println); 
		JSONArray merged= toJSONArray(result.stream().distinct().collect(Collectors.toList()));
		response.setData(merged);
		writeJson(response.getId().toString(), merged, "merge.json");
		File outputDir = new File(Paths.get("").toAbsolutePath().toString() +File.separator+ response.getId() );
		ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
		
		convertJsonToExcel(outByteStream, outputDir);
		return response;
	}

	private JSONArray toJSONArray(List<JSONObject> dataList) {
		JSONArray jArray = new JSONArray();
		dataList.stream().forEach(d ->{
			jArray.add(d);
		});
		return jArray;
	}

	public static List<JSONObject> matchStream(List<JSONObject> listOne, List<JSONObject> listTwo, String fieldName)
	{
		
	    List<JSONObject> listOneList = listOne.stream()
	    .filter(two -> listTwo.stream()
	        .anyMatch(one -> one.containsKey(fieldName) && two.get(fieldName).equals(one.get(fieldName))))
	    .map(s -> {
	    	s.put("actionType","EQUAL");
	    	return s;
	    })
	    .collect(Collectors.toList());
	    return listOneList;
	}
	
	public static List<JSONObject> addedStream(List<JSONObject> listOne, List<JSONObject> listTwo, String fieldName)
	{
		
	    List<JSONObject> listOneList = listOne.stream()
	    .filter(two -> listTwo.stream()
	        .anyMatch(one -> one.containsKey(fieldName) && two.get(fieldName).equals(one.get(fieldName)))==false)
	    .map(s -> {
	    	s.put("actionType","REMOVED");
	    	return s;
	    })
	    .collect(Collectors.toList());
	    return listOneList;
	}
	
	public static List<JSONObject> removedStream(List<JSONObject> listOne, List<JSONObject> listTwo, String fieldName)
	{
		
	    List<JSONObject> listOneList = listTwo.stream()
	    .filter(two -> listOne.stream()
	        .anyMatch(one -> one.containsKey(fieldName) && two.get(fieldName).equals(one.get(fieldName)))==false)
	    .map(s -> {
	    	s.put("actionType","ADDED");
	    	return s;
	    })
	    .collect(Collectors.toList());
	    return listOneList;
	}
	

	private void convertJsonToExcel(ByteArrayOutputStream outputStream, File srcDir) {
		ExcelResponse response = new ExcelResponse();
		List<JSONObject> jsonObjects = new ArrayList<>();
		JSONParser parser = new JSONParser();
		JSONArray jsonArray;
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Report");
		try {
			FileReader fr =new FileReader(srcDir.getAbsolutePath() + File.separator+"merge.json");
			jsonArray = (JSONArray) parser.parse(fr) ;
			 Integer rowCount = 0;
			 
			 Row row = sheet.createRow(rowCount);
			 int colCounter=0;
			 for(Iterator iterator = ((JSONObject)jsonArray.get(0)).keySet().iterator(); iterator.hasNext();) {
				    String key = (String) iterator.next();
				   
				    Cell cell1 = row.createCell(colCounter++);
		            cell1.setCellValue(key);
				} 
			 
			  for (int i = 0; i < jsonArray.size(); i++) {  
				  JSONObject jsonObject = (JSONObject)jsonArray.get(i);
				  String actionKey = jsonObject.get("actionType").toString();
				 
					  row = sheet.createRow(++rowCount);			  
					    
					  colCounter=0;
					  for(Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
						  String key = (String) iterator.next();

						  System.out.println(jsonObject.get(key));
						  Cell cell1 = row.createCell(colCounter++);
						  CellStyle style=cell1.getCellStyle();
						  if(actionKey.equalsIgnoreCase("REMOVED")){
							  System.out.println(rowCount+" -" +actionKey +"-"+IndexedColors.RED);
						    style.setFillBackgroundColor(IndexedColors.RED.getIndex());
						    setCellColorAndFontColor(workbook,cell1, IndexedColors.RED, IndexedColors.WHITE,actionKey);
						  } else  if(actionKey.equalsIgnoreCase("ADDED")){
							  System.out.println(rowCount+" -" +actionKey +"-"+IndexedColors.GREEN);
							  style.setFillBackgroundColor(IndexedColors.GREEN.getIndex()); 
							  setCellColorAndFontColor(workbook,cell1, IndexedColors.GREEN, IndexedColors.WHITE,actionKey);
						  }
						  cell1.setCellValue(jsonObject.get(key).toString());
					  }  
				  
			
			}
			 // workbook.write(outputStream);
			  try (FileOutputStream fileOutputStream = new FileOutputStream("Report.xlsx")) {
		            workbook.write(fileOutputStream);
		        }
			  
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void setCellColorAndFontColor(XSSFWorkbook wb,Cell cell, IndexedColors FGcolor, IndexedColors FontColor,String actionType ){	    
		XSSFCellStyle cs1 = wb.createCellStyle();
		XSSFFont f = wb.createFont();
		f.setBold(true);
		if(actionType.equalsIgnoreCase("REMOVED")){
			f.setColor(IndexedColors.RED.getIndex());
		} else {
			f.setColor(IndexedColors.GREEN.getIndex());
		}
		cs1.setFont(f);
		cs1.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
	    cell.setCellStyle(cs1);
	}
	
	public void writeJson(String uuid, JSONObject jsonData, String fileName) {
		FileWriter file = null;
		try {
			boolean exist=new File(Paths.get("").toAbsolutePath().toString() +File.separator+ uuid ).mkdirs();
			System.out.println(exist);
			file = new FileWriter(Paths.get("").toAbsolutePath().toString()+File.separator+ uuid + File.separator+fileName);
			file.write(jsonData.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void writeJson(String uuid, JSONArray jsonData, String fileName) {
		FileWriter file = null;
		try {
			boolean exist=new File(Paths.get("").toAbsolutePath().toString() +File.separator+ uuid ).mkdirs();
			System.out.println(exist);
			file = new FileWriter(Paths.get("").toAbsolutePath().toString()+File.separator+ uuid + File.separator+fileName);
			file.write(jsonData.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

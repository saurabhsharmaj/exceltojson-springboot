package com.example.Excel2Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
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
		response.setData(result.stream().distinct().collect(Collectors.toList()));
		return response;
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
	
	private static List<Column> createCompareColumn() {
		List<Column> list = new ArrayList<Column>();
		Column col1= new Column();
		col1.setLocalColName("FirstName");
		col1.setRemoteColName("FirstName");
		list.add(col1);
		
		Column col2= new Column();
		col2.setLocalColName("LastName");
		col2.setRemoteColName("LastName");
		list.add(col2);
		return list;
	}

	public static void main(String[] args) throws Exception {

		List<JSONObject> local = excelToJSON(new FileInputStream(new File("D:\\projects\\exceltojson-springboot\\ll.xlsx")));
		List<JSONObject> remote = excelToJSON(new FileInputStream(new File("D:\\projects\\exceltojson-springboot\\rr.xlsx")));
		ExcelRequest exlReq= new ExcelRequest();
		exlReq.setColumn(createCompareColumn());
		JSONObject excel = new JSONObject();
		//excel.put("compare", exlReq);
		//excel.put("local", local);
		//excel.put("remote", remote);
		
		List<JSONObject> result=compareExcel(local,remote,exlReq.getColumn());
		excel.put("result", result);
		System.out.println(excel);
		
	}

	

	private static List<JSONObject> compareExcel(List<JSONObject> local, List<JSONObject> remote, List<Column> columns) {

		List<JSONObject> result = new ArrayList<JSONObject>();
		
		List<JSONObject> added = local.stream()
	    	    .filter(ll -> remote.stream()
	    		        .noneMatch(rr->fetchAdded(rr,ll,columns)))
	    		    .map(s -> {
	    		    	s.put("actionType","ADDED");
	    		    	return s;
	    		    })
	    		    .collect(Collectors.toList());
	    result.addAll(added);
	    
	    List<JSONObject> equal = local.stream()
	    	    .filter(ll -> remote.stream()
	    		        .anyMatch(rr->fetchEqual(rr,ll,columns)))
	    		    .map(s -> {
	    		    	s.put("actionType","EQUAL");
	    		    	return s;
	    		    })
	    		    .collect(Collectors.toList());
	    result.addAll(equal);
	    
	    List<JSONObject> modified = local.stream()
	    	    .filter(ll -> remote.stream()
	    		        .anyMatch(rr->fetchModified(rr,ll,columns)))
	    		    .map(s -> {
	    		    	s.put("actionType","MODIFIED");
	    		    	return s;
	    		    })
	    		    .collect(Collectors.toList());
	    
	    result.addAll(modified);
	    

	    return result.stream().distinct().collect(Collectors.toList());
	}
	
	private static boolean fetchAdded(JSONObject local, JSONObject remote, List<Column> columns) {
		return columns.stream().allMatch(c-> local.get(c.getLocalColName()).equals(remote.get(c.getRemoteColName())));
	}
	

	private static boolean fetchModified(JSONObject local, JSONObject remote, List<Column> columns) {
		if(fetchEqual(local, remote, columns)==false) {
			return columns.stream().anyMatch(c-> local.get(c.getLocalColName()).equals(remote.get(c.getRemoteColName())));
		}
		return false;
	}
	
	private static boolean fetchEqual(JSONObject local, JSONObject remote, List<Column> columns) {		
		return columns.stream().allMatch(c-> local.get(c.getLocalColName()).equals(remote.get(c.getRemoteColName())));
	}

	private static List<JSONObject> excelToJSON(FileInputStream local) throws IOException {
		List<JSONObject> list= new ArrayList<JSONObject>();
		XSSFWorkbook workbook = new XSSFWorkbook(local);		
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
			list.add(rowJsonObject);
		}
		return list;
	}
	
	
	

}

package com.example.Excel2Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ExcelController {

	@PostMapping("/excel")
	public List<JSONObject> getExcel(@RequestParam("local") MultipartFile local,@RequestParam("remote") MultipartFile remote, @RequestParam("name") String columnName) {
		return excel2Json(new MultipartFile[] {local,remote},columnName);
	}

	public List<JSONObject> excel2Json(MultipartFile[] data,String matchColumnName) {

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
		return result.stream().distinct().collect(Collectors.toList());
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
	    	s.put("actionType","ADDED");
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
	    	s.put("actionType","REMOVED");
	    	return s;
	    })
	    .collect(Collectors.toList());
	    return listOneList;
	}
	

	

}

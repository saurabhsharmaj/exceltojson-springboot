package com.example.Excel2Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

@RestController
public class ExcelController {

	@Autowired
	ObjectMapper mapper;

	TypeReference<HashMap<String, Object>> type = new TypeReference<HashMap<String, Object>>() {
	};

	@PostMapping("/excel")
	public String getExcel(@RequestParam("data") MultipartFile[] data) {
		excel2Json(data);
		return "Success";
	}

	public void excel2Json(MultipartFile[] data) {

		List<JSONObject> dataList = new ArrayList<>();
		List<JSONObject> dataList1 = new ArrayList<>();

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
					dataList.add(rowJsonObject);
				} else {
					dataList1.add(rowJsonObject);
				}

			}
		}
		System.out.println("first file : " + dataList);
		System.out.println("second file : " + dataList1);

		try {
			String leftJson = "{\"name\":\"John\", \"age\":30, \"car\":null}";
			String rightJson = "{\"name\":\"John\", \"age\":30, \"car\":null}";
			Map<String, Object> leftMap = mapper.readValue(leftJson, type);
			Map<String, Object> rightMap = mapper.readValue(rightJson, type);

			MapDifference<String, Object> compareJsonObject = compareJsonObject(leftMap, rightMap);
			System.out.println(compareJsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<JSONObject> result=createSharedListViaStream(dataList, dataList1, "id");
		result.stream().distinct().forEach(System.err::println); 
	}

	public static List<JSONObject> createSharedListViaStream(List<JSONObject> listOne, List<JSONObject> listTwo, String fieldName)
	{
		
	    List<JSONObject> listOneList = listOne.stream()
	    .filter(two -> listTwo.stream()
	        .anyMatch(one -> one.containsKey(fieldName) && two.get(fieldName).equals(one.get(fieldName))))
	    .collect(Collectors.toList());
	    return listOneList;
	}
	
	MapDifference<String, Object> compareJsonObject(Map<String, Object> leftMap, Map<String, Object> rightMap) {
		return Maps.difference(leftMap, rightMap);
	}

}

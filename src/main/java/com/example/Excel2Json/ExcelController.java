package com.example.Excel2Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	}

}

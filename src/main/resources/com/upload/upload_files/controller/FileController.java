package com.upload.upload_files.controller;

import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.upload.upload_files.service.FileService;

@RestController
public class FileController {

	@Autowired
	private FileService fileService;

	@PostMapping("/uploadfiles")
	public String uploadFiles(@RequestParam("files") MultipartFile[] files) {

		Arrays.asList(files).stream().forEach(file -> fileService.uploadFile(file));

		return "Successsfully uploaded";
	}

	@PostMapping("/excel")
	public String excelReader(@RequestParam("file") MultipartFile[] excel) {

		for (int a = 0; a < excel.length; a++) {
			XSSFWorkbook workbook = null;
			try {
				workbook = new XSSFWorkbook(excel[a].getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			XSSFSheet sheet = workbook.getSheetAt(0);

			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = sheet.getRow(i);
				for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
					DataFormatter dataFormatter = new DataFormatter();
					System.out.print(dataFormatter.formatCellValue(row.getCell(j)) + " ");
				}
				System.out.println("");
			}

			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}
		return "Success";
	}
}

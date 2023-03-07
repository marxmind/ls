package com.italia.municipality.lakesebu.controller;
import java.io.FileInputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QRCodeCitizen {

	private FileInputStream f1;
	private FileInputStream f2;
	private FileInputStream f3;
	
}

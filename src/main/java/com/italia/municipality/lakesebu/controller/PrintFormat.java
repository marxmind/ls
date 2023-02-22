package com.italia.municipality.lakesebu.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PrintFormat {
	private int id;
	private String name;
	private boolean isSelected;
	private String fileName;
}

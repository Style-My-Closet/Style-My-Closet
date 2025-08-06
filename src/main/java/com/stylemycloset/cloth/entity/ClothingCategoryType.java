package com.stylemycloset.cloth.entity;

public enum ClothingCategoryType {
  TOP("상의"),
  BOTTOM("하의"),
  DRESS("원피스"),
  OUTER("아우터"),
  SHOES("신발"),
  ACCESSORY("악세사리");
  //추가 예정
  private final String description;

  ClothingCategoryType(String description) {
    this.description = description;
  }


}
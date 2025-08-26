package com.stylemycloset.clothes.entity.clothes;


public enum ClothesType { // 이 부분은 추가 가능한지 봐야됨
  TOP,
  BOTTOM,
  DRESS,
  OUTER,
  UNDERWEAR,
  ACCESSORY,
  SHOES,
  SOCKS,
  HAT,
  BAG,
  SCARF,
  ETC;

  public static ClothesType from(String type) {
    for (ClothesType clothesType : ClothesType.values()) {
      if (clothesType.name().equalsIgnoreCase(type)) {
        return clothesType;
      }
    }
    return ETC;
  }

}
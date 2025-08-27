package com.stylemycloset.clothes.entity.clothes;


public enum ClothesType {
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
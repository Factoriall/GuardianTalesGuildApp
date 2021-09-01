package org.techtown.gtguildraid.models;

public class BossImage {
     final String imgName;
     final int imgId;
     final String bossName;

     public BossImage(String imgName, int imgId, String bossName) {
         this.imgName = imgName;
         this.imgId = imgId;
         this.bossName = bossName;
     }

     public String getImgName() {
         return imgName;
     }

     public int getImgId() {
         return imgId;
     }

     public String getBossName() { return bossName; }
}

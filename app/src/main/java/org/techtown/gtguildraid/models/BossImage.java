package org.techtown.gtguildraid.models;

public class BossImage {
     final String imgName;
     final int imgId;

     public BossImage(String imgName, int imgId) {
         this.imgName = imgName;
         this.imgId = imgId;
     }

     public String getImgName() {
         return imgName;
     }

     public int getImgId() {
         return imgId;
     }
}

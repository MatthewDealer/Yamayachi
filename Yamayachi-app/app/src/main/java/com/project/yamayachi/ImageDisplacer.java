package com.project.yamayachi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;

import androidx.annotation.RequiresApi;


public class ImageDisplacer {
    Bitmap displacement_img;
    float[][] zDepth;
    Bitmap img;
    Bitmap map;
    int width = 0;
    int height = 0;

     @RequiresApi(api = Build.VERSION_CODES.Q)
     public ImageDisplacer(Bitmap img, Bitmap map){
         this.img = img;
         this.map = map;
         width = img.getWidth();
         height = img.getHeight();
         displacement_img = img.copy(Bitmap.Config.ARGB_8888,true);
         zDepth = new float[img.getWidth()][img.getHeight()];
         resetZDepth();

     }

     @RequiresApi(api = Build.VERSION_CODES.Q)

     public void printBitmap(Bitmap toPrint){
         int width = toPrint.getWidth();
         int height = toPrint.getHeight();

         System.out.print("Width = " + width + ", Height = " + height);
         System.out.println("\n_______________________________________");

         int[] colors = new int[width*height];
         img.getPixels(colors,0,width,0,0,width,height);
         for(int i =0; i<colors.length; i++){
            System.out.print(colors[i]);
         }
         System.out.println("_______________________________________");
     }

     private void resetZDepth(){
         for(int i = 0; i < width; i++)
             for(int j = 0; j < height; j++)
                 zDepth[i][j] =  -1;
     }

    @RequiresApi(api = Build.VERSION_CODES.Q)
     public void displacement (int x, int y){
         //resetZDepth();
         displacement_img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         for(int i = 0; i < width; i++) {
             for(int j = 0; j < height; j++) {
                 if (img.getColor(i, j).alpha() !=0) {
                     float luminance = (float) (map.getColor(i, j).luminance() -0.2);
                     int new_x = Math.min(width - 1, Math.max(0, Math.round(i + x * luminance)));
                     int new_y = Math.min(height - 1, Math.max(0, Math.round(j + y * luminance)));
                    // if (zDepth[new_x][new_y] < luminance) {
                         displacement_img.setPixel(i, j, img.getPixel(new_x,new_y));
                        // zDepth[new_x][new_y] = luminance;
                     //}
                 }
             }
         }
     }

     @RequiresApi(api = Build.VERSION_CODES.Q)
     public Bitmap getDisplacementImage(int x, int y){
         displacement(x, y);
         return displacement_img;
     }
}

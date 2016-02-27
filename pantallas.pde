void splash_screen() {
//  KetaiAlertDialog.popup(this, "EKG Visor 0.1a", "(c) 2016 JA Barios, HeartTrack");
}

//cabecero en draw_REC
void pinta_textos(int cx,int cy,color clr) {
  fill(clr);                            
  text("EKG visor 0.1a (c) HeartTrack", cx, cy);     
}

// barra derecha
void pinta_borde() {
  fill(color(255, 255, 200));
  rect(anchoPantalla-40,altoPantalla/2,80,altoPantalla);
}


void pinta_barrapie() {
  pushStyle();
  textSize(16);
  textAlign(CENTER);
  fill(255);
  rect(width/2, altoPantalla-12, width, 32);
  fill(0);
  int tasaRefresc=60; 
  int marcadores=10;
  int intervalo_marcadores=10;
  int current_time=1234;
  int proximo_toc=122;
  text(""+nf((int)estHz, 2)+" "+nf(gFrecuenciaMuestra2, 2)+" "+nf(cnt2, 6)+" "+nf((int)zoom, 3)+" "+nf(((int)(1000.0*gDuracionPantalla)),4)+" "+gTextoMensaje+" r:"+HTversion, anchoPantalla/2, altoPantalla-8);
  if(!isBT){
    fill(color(128,0,0));
    rect(16, altoPantalla-12, 32, 32);
    fill(color(255,255,255));
    text("SG", 16, altoPantalla-16);
  } else {
    fill(color(0,128,128));
    rect(16, altoPantalla-12, 32, 32);
    fill(color(255,255,255));
    text("BT", 16, altoPantalla-16);
  }  
  if(isSonidoOn){
    fill(color(128,0,0));
    rect(48, altoPantalla-12, 32, 32);
    fill(color(255,255,255));
    text("SS", 48, altoPantalla-16);
  } else {
    fill(color(0,128,128));
    rect(48, altoPantalla-12, 32, 32);
    fill(color(255,255,255));
    text("  ", 48, altoPantalla-16);
  }
  text(gTextoMensaje, width/2, altoPantalla-16);
  
  popStyle();

}


// pinta pantalla de teclado
void drawUI()
{
  pushStyle();
  textAlign(CENTER);
  rectMode(CENTER);
  ellipseMode(CENTER);  

  background(0);
  textAlign(CENTER);
  fill(0);
   stroke(color(255, 255, 200));

  int offset=anchoTeclado/6;
  rect(offset, 60, -2+anchoTeclado/3, 120);
  rect(anchoTeclado/3+offset, 60, -2+anchoTeclado/3, 120);
  rect((anchoTeclado/3)*2+offset, 60, -2+anchoTeclado/3, 120);
  rect(offset, 160, -2+anchoTeclado/3, 120);
  rect(anchoTeclado/3+offset, 160, -2+anchoTeclado/3, 120);
  rect((anchoTeclado/3)*2+offset, 160, -2+anchoTeclado/3, 120);
  rect(offset, 260, -2+anchoTeclado/3, 120);
  rect(anchoTeclado/3+offset, 260, -2+anchoTeclado/3, 120);
  rect((anchoTeclado/3)*2+offset, 260, -2+anchoTeclado/3, 120);

  textSize(25);

  pinta_textos(anchoTeclado/2,altoPantalla-10,color(255,255,255)); 
  fill(255);
  fill(color(255, 255, 200));

  if(isBT){
      text("Simula", offset, 60);
  }else{
      text("BT", offset, 60);
  }
  text("BT unpar", offset+anchoTeclado/3, 60); 
  text("About", offset+anchoTeclado/3*2, 60);
  text("BT id", offset, 160);
  text("Acelera", offset+anchoTeclado/3, 160); 
  text("Frena", offset+anchoTeclado/3*2, 160);
  text("Sonido", offset, 260);
  text("", offset+anchoTeclado/3, 260); 
  text("", offset+anchoTeclado/3*2, 260);
  pinta_borde(); 
 
  image(p, (width-40)/2- p.width*2, 260+((height-260)/2)-p.height*2, p.width*4, p.height*4);
  pinta_barrapie();
  stroke(250);
  text(HTversion, altoPantalla-40, 2);

  textSize(32);
  textAlign(CENTER);
  text(BT_id, anchoPantalla/2,360);
  popStyle();
 
}
void mensajeAbajo(String cadena)
{
 //  textSize(16);
 //  text(cadena, 1, altoPantalla-20); 
 gTextoMensaje=cadena;
}  

void calibraScope(){
        mensajeAbajo("calibrando pantalla");
         if ((startTime == 0) && (valuesReceived > 0)){
          valuesReceived = 0; 
          startTime = millis();
        }
        calibrationFrames--;
        if (calibrationFrames == 0) {
          calibrateFrameRate(); 
        }
 }
 
 void drawScope(){
         fill(250); // semi-transparent white
       rect(0, lineaMenu, screenWid,height-lineaMenu);

       pinta_textos(anchoPantalla/2,20,color(0,0,0)); 
       mensajeAbajo("mostrando señales");
    
//      background(250); // background 0 = black, 255= white
      stroke(color(255,0,0)); // barra ancha separando menu superior
      strokeWeight(4);
      line(0,lineaMenu,screenWid,lineaMenu);
        
      //next: vertical lines for seconds...
      stroke(120);
      strokeWeight(1);
      
      float xpos = tickSpacing;
      while (xpos < width) {  
        line(xpos,lineaMenu,xpos,screenHt);  
        xpos = xpos + tickSpacing;
      }
   
   
   for (int i = 0; i < gOscChannels; i++) {
        stroke(lineColorsRGB[i][0],lineColorsRGB[i][1],lineColorsRGB[i][2]);
        int offset=altoBarrido*i+altoBarrido/2+lineaMenu;
        if(!autoScale){
              arr_int ss= new arr_int(wm1);
              for (int x=2; x<wm1; x++) ss.set(x,gEscala*values[i][x]);
              ss.remove_offset();
             for (int x=2; x<wm1; x++) {
                int y1=ss.get(x-1);
                int y2=ss.get(x);
                line (x-1,  offset+float(y1)/float(gOscChannels+2), x, offset+float(y2)/float((gOscChannels+2)));
             }
        } else {
              arr_int ss= new arr_int(wm1);
              for (int x=2; x<wm1; x++) ss.set(x,gEscala*values[i][x]);
              ss.remove_offset();
            
             for (int x=2; x<wm1; x++) {
                 int y1=ss.get(x-1);
                 int y2=ss.get(x);
                 line (x-1,  offset+y1/(gOscChannels+2), x, offset+y2/(gOscChannels+2));
             }
        }
      }
      
      //draw the leading edge line
      stroke(255,255,0);
      line(cnt,halfScreenHt-positionHt,cnt,halfScreenHt+positionHt);  

        fill(250); // semi-transparent white
        rect(0, 0, screenWid,lineaMenu);
        int[] marcas=new int[200];
        int sss=values[1].length;
        fill(0);
  
      //pinta barra alta
        fill(0); 
        textFont(f,10);
        text("FC",39,lineaMenu/3-5);
        textFont(f,32);
        text(gFrecuenciaCardiaca,30,5+2*lineaMenu/3);
        textFont(f,7);
        text("bpm",39,14+2*lineaMenu/3);
        image(p, width-p.width*1.3, 0, p.width*1.3, p.height);
        pinta_barrapie();
 
 }  
 
 
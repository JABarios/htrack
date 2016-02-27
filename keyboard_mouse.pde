void efecto(){
    fill(0);
    rect(0, 0, anchoPantalla, altoPantalla); // efecto al tocar la tecla
}

void mousePressed() {
  // en ambas pantallas, el borde derecho cambia de modo
  zoom=gEscala;
    
  if ( mouseX > anchoPantalla-80) {
    isUI=!isUI;
    return;
  }
//  vibe.vibrate(50);
//  beeping();

  if (isUI) {
    efecto();
  }

  // zoom tocando mitad superior e inferior de pantalla UI
  if (!isUI && mouseY < altoPantalla/2) {
    efecto();
    if (zoom<10)zoom+=0.5;
  }
  if (!isUI && mouseY > altoPantalla/2) {
    if (zoom>0.2)zoom-=0.5;
    efecto();
  }
  // en modo UI, teclado sencillo que llama distintas funciones
  if (isUI && mouseY < 120)
  {
    if (mouseX < anchoTeclado/3) { //bT
      isConfiguring = !isConfiguring;
      isBT=!isBT;
//      isUI=false;
      return;
    } else if (mouseX > anchoTeclado/3 && mouseX < anchoTeclado*2/3) {
      byte[] data = {'c'};                 // desparear
    //  bt.broadcast(data);
      isConfiguring = false;
      isBT=false;
      return;
    } else {
       byte[] data = {'h','e','a','r','t','t','r','a','c','k','\r'};
       bt.broadcast(data);
       m_print("Enviado: " + data);
       return;
    }
  }
//fila 2
  if (isUI && (mouseY > 120 && mouseY < 240) )
  {
    if (mouseX < anchoTeclado/3) {
      getBluetoothInformation();
      if (isBT) {
        byte[] data = {'a'};                 // reset frec
     //   bt.broadcast(data);
      } else {
     //   frec_simulada=3;
      }
      return;
    } else if (mouseX > anchoTeclado/3 && mouseX < anchoTeclado-(anchoTeclado/3)) {
      if (isBT) {
//        byte[] data = {'k'};                 // inc frec
      //  bt.broadcast(data);
      } else {
        gFrecuenciaMuestra2+=1;
        if (gFrecuenciaMuestra2>=12)gFrecuenciaMuestra2=12;
      }
      return;
    } else {
      if (isBT) {
        byte[] data = {'b'};                 // dec frec
      //  bt.broadcast(data);
      } else {
        gFrecuenciaMuestra2-=1;
        if (gFrecuenciaMuestra2<=0.5)gFrecuenciaMuestra2=1;
      }
      return;
   }
  } 
  if (isUI && (mouseY > 240 && mouseY < 360) )
  {
    if (mouseX < anchoTeclado/3) {
       isSonidoOn=!isSonidoOn;      
       return;
    } else if (mouseX > anchoTeclado/3 && mouseX < anchoTeclado-(anchoTeclado/3)) {
     //  last_toc+=5;
    // salto_fase=100;
      return;
    } else {
      
      return;
   }
  }
}

void mouseReleased() {
//  scaleBar.release();
}

void keyPressed() {
  switch(key) {
  case 's':    // pressing 's' or 'S' will take a jpg of the processing window
  case 'S':
    saveFrame("Memboost-####.jpg");    // take a shot of that!
    break;

  default:
    break;
  }
}
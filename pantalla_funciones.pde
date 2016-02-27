void pantalla_inicia() 
{
 /* if (gModo==2)
    //frame.setTitle("Heart track 0.1"); //Set the frame title to the frame rate
  else
    //
 */
  frameRate(gFrameRate); //refresh screen 60 times per second

  residualRawData = new int[maxResidualBytes];
  //setup vertical scaling
  channelMin= new int[kOscMaxChannels];
  channelMax= new int[kOscMaxChannels];
  channelSlope= new float[kOscMaxChannels];
  channelIntercept= new int[kOscMaxChannels];
 
  for (int c=0;c<kOscMaxChannels;c++) {
    if (offsetTraces) 
      channelIntercept[c] = Margin+ (c * 2);
    else
      channelIntercept[c] = Margin;
    channelMin[c] = -8388608; //minimum 24-bit signed integer
    channelMax[c] = 8388607; //maximum 24-bit signed integer
    //autoScaleChannel(c);
    if (autoScale) { //set impossible values to detect true variability
      channelMin[c] = 8388607;
      channelMax[c] = -8388608;        
    }
  }
  if (gOscChannels > lineColorsRGB.length) {
    println("Error: you need to specify more colors to the array lineColorsRGB.");
    exit();   
  } 
  if (gOscChannels > kOscMaxChannels | gOscChannels < 1) {
    print("Error: you requested "); print(gOscChannels); print(" channels but this software currently only supports ");println(kOscMaxChannels);
    exit();   
  } 

}

void calibrateFrameRate()
{
  if (valuesReceived < 1) {
    println("Error: No samples detected: either device is not connected or serialPortNumber is wrong.");
    exit();
  }
  mensajeAbajo("calibrando pantalla");
  float plotEveryNthSample;
  plotEveryNthSample = (gGraphTotalTimeSec/float(screenWid)* float(gOscHz));
  if (plotEveryNthSample > 1) plotEveryNthSample = round(plotEveryNthSample);
  if (plotEveryNthSample == 1) plotEveryNthSample = 1; 
  pixelsPerSample = 1 / plotEveryNthSample;
  estHz = (1000*valuesReceived)/(millis()-startTime);
  if ((tickSpacing == 0) || (screenWid == 0) || (plotEveryNthSample ==0) || (gOscHz == 0) ) //avoid divide by zero
    tickSpacing = screenWid / 4;
  else {
    tickSpacing = screenWid /  (((screenWid *plotEveryNthSample)/ gOscHz)/tickSpacing);
  }
  print("Requested ");  print(gOscHz); print("Hz, so far we have observed "); print(estHz); println("Hz");
  print ("Displaying  ");  print(pixelsPerSample); print(" pixels per sample, so the screen shows "); print((screenWid *plotEveryNthSample)/ gOscHz); println(" Sec");
  gDuracionPantalla=(screenWid *plotEveryNthSample)/ gOscHz;
} //calibrateFrameRate()
package io.mosip.mds.helper;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FingerPrintImageExtractor {

    public List<ExtractDTO> extractFingerImageData(byte[] decodedBioValue)  {
        List<ExtractDTO> extracts = new ArrayList<>();

        try(DataInputStream din = new DataInputStream(new ByteArrayInputStream(decodedBioValue))) {

            byte[] format = new byte[4];
            din.read(format, 0, format.length);
            System.out.println("format >>>> " + new String(format));

            int version = din.readInt();
            int recordLength = din.readInt();
            short noOfRepresentations = din.readShort();
            byte certificationFlag = din.readByte();
            byte noOfFingersOrPalms = din.readByte();

            for(int i=0; i<noOfFingersOrPalms; i++) {
                int representationLength = din.readInt();
                System.out.println("representationLength >>> " + representationLength);

                byte[] representationBlock = new byte[representationLength-4];
                din.read(representationBlock, 0, representationBlock.length);

                try(DataInputStream rdin = new DataInputStream(new ByteArrayInputStream(representationBlock))) {
                    byte[] captureDetails = new byte[14];
                    rdin.read(captureDetails, 0, captureDetails.length);

                    //qualityBlock
                    byte noOfQualityBlocks = rdin.readByte();
                    if(noOfQualityBlocks > 0) {
                        byte[] qualityBlocks = new byte[noOfQualityBlocks * 5];
                        rdin.read(qualityBlocks, 0, qualityBlocks.length);
                    }

                    byte noOfCertificationBlock = rdin.readByte();
                    if(noOfCertificationBlock > 0) {
                        byte[] certBlocks = new byte[noOfCertificationBlock * 3];
                        rdin.read(certBlocks, 0, certBlocks.length);
                    }

                    byte finger = rdin.readByte(); // finger/palm position
                    System.out.println("Finger position >>>>>> " + finger);

                    byte representationNumber = rdin.readByte();
                    System.out.println("representationNumber >>>>>> " + representationNumber);

                    byte[] otherDetails = new byte[10];
                    rdin.read(otherDetails, 0, otherDetails.length);

                    byte imageCompressionAlgo = rdin.readByte();
                    System.out.println("imageCompressionAlgo >>> " + imageCompressionAlgo);
                    byte impressionType = rdin.readByte();

                    byte[] lineDetails = new byte[4];
                    rdin.read(lineDetails, 0, lineDetails.length);

                    int imageDatalength = rdin.readInt();
                    System.out.println("imageDatalength >>> " + imageDatalength);

                    byte[] image = new byte[imageDatalength];
                    rdin.read(image, 0, image.length);

                    extracts.add(new ExtractDTO(image, getSegmentName(finger), imageCompressionAlgo == 3 ? "jpg" :
                            imageCompressionAlgo>3 && imageCompressionAlgo<6 ? "jp2" :
                                    imageCompressionAlgo == 6 ? "png" : "unknown", representationNumber));
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return extracts;
    }

    private String getSegmentName(int position) {
        String name = "unknown";
        switch (position) {
            case 1: name = "rigthThumb"; break;
            case 2: name = "rigthIndex"; break;
            case 3: name = "rigthMiddle"; break;
            case 4: name = "rigthRing"; break;
            case 5: name = "rigthLittle"; break;
            case 6: name = "leftThumb"; break;
            case 7: name = "leftIndex"; break;
            case 8: name = "leftMiddle"; break;
            case 9: name = "leftRing"; break;
            case 10: name = "leftLittle"; break;
        }
        return name;
    }
}

package io.mosip.mds.helper;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IrisImageExtractor {

    public List<ExtractDTO> extractIrisImageData(byte[] decodedBioValue) {
        List<ExtractDTO> extracts = new ArrayList<>();

        try(DataInputStream din = new DataInputStream(new ByteArrayInputStream(decodedBioValue))) {
            System.out.println("Started processing bio value : " + new Date());

            //general header parsing
            byte[] formatIdentifier = new byte[4];
            din.read(formatIdentifier, 0, formatIdentifier.length);
            System.out.println("formatIdentifier >>>> " + new String(formatIdentifier));

            int version = din.readInt();
            int recordLength = din.readInt();
            short noOfRepresentations = din.readShort();
            byte certificationFlag = din.readByte();

            // 1 -- left / right
            // 2 -- both left and right
            // 0 -- unknown
            byte noOfIrisRepresented = din.readByte();

            for(int i=0; i<noOfRepresentations; i++) {
                //Reading representation header
                int representationLength = din.readInt();

                byte[] captureDetails = new byte[14];
                din.read(captureDetails, 0, captureDetails.length);

                //qualityBlock
                byte noOfQualityBlocks = din.readByte();
                if(noOfQualityBlocks > 0) {
                    byte[] qualityBlocks = new byte[noOfQualityBlocks * 5];
                    din.read(qualityBlocks, 0, qualityBlocks.length);
                }

                short representationSequenceNo = din.readShort();

                // 0 -- undefined
                // 1 -- right
                // 2 - left
                byte eyeLabel = din.readByte();

                byte imageType = din.readByte(); // cropped / uncropped

                // 2 -- raw
                // 10 -- jpeg2000
                // 14 -- png
                byte imageFormat = din.readByte();

                byte[] otherDetails = new byte[24];
                din.read(otherDetails, 0, otherDetails.length);

                int imageLength = din.readInt();
                byte[] image = new byte[imageLength];
                din.read(image, 0, image.length);

                //TODO - check if segment provided to read is same as eyeLabel
                extracts.add(new ExtractDTO(image, eyeLabel == 1 ? "right" : eyeLabel == 2 ? "left" : "unknown",
                        imageFormat == 10 ? "jp2" : imageFormat == 14 ? "png" : "unknown", representationSequenceNo));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return  extracts;
    }

    private void createImageFile(byte[] image, String segment, String format) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(new File(String.format("/home/anusha/data/%s.%s",
                segment, format)))) {
            fos.write(image);
            fos.flush();
        }
    }
}

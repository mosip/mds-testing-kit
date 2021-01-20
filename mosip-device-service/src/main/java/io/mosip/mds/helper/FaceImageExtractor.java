package io.mosip.mds.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FaceImageExtractor {

    private static final Logger logger = LoggerFactory.getLogger(FaceImageExtractor.class);

    public List<ExtractDTO> extractFaceImageData(byte[] decodedBioValue) {
        List<ExtractDTO> extracts = new ArrayList<>();

        try(DataInputStream din = new DataInputStream(new ByteArrayInputStream(decodedBioValue))) {
            logger.info("Started processing bio value : {}", new Date());

            //Parsing general header
            byte[] format = new byte[4];
            din.read(format, 0, 4);
            logger.debug("format >>>>>>>>> {} ",new String(format));

            byte[] version = new byte[4];
            din.read(version, 0, 4);
            logger.debug("version >>>>>>>>> {}", new String(version));

            int recordLength = din.readInt(); // 4 bytes
            logger.debug("recordLength >>>>>>>>>" + recordLength);

            short numberofRepresentionRecord = din.readShort();
            logger.debug("numberofRepresentionRecord >>>>>>>>>" + numberofRepresentionRecord);

            //NOTE: No certification schemes are available for this part of ISO/IEC 19794.
            byte certificationFlag = din.readByte();
            logger.debug("certificationFlag >>>>>>>>>" + certificationFlag);

            byte[] temporalSequence = new byte[2];
            din.read(temporalSequence, 0, 2);
            logger.debug("temporalSequence >>>>>>>>>" + new String(temporalSequence));

            //Parsing representation header
            int representationLength = din.readInt();
            logger.debug("representationLength >>>>>>>>>" + representationLength);

            byte[] representationData = new byte[representationLength-4];
            din.read(representationData, 0, representationData.length);

            try(DataInputStream rdin = new DataInputStream(new ByteArrayInputStream(representationData))) {
                byte[] captureDetails = new byte[14];
                rdin.read(captureDetails, 0, 14);

                byte noOfQualityBlocks = rdin.readByte();
                logger.debug("noOfQualityBlocks >>>>>>>>>" + noOfQualityBlocks);

                if(noOfQualityBlocks > 0) {
                   byte[] qualityBlocks = new byte[noOfQualityBlocks * 5];
                   rdin.read(qualityBlocks, 0, qualityBlocks.length);
                }

                short noOfLandmarkPoints = rdin.readShort();
                logger.debug("noOfLandmarkPoints >>>>>>>>>" + noOfLandmarkPoints);
                //read next 15 bytes
                byte[] facialInformation = new byte[15];
                rdin.read(facialInformation, 0, 15);

                //read all landmarkpoints
                if(noOfLandmarkPoints > 0) {
                    byte[] landmarkPoints = new byte[noOfLandmarkPoints * 8];
                    rdin.read(landmarkPoints, 0, landmarkPoints.length);
                }

                byte faceType = rdin.readByte();
                logger.debug("faceType >>>>>>>>>" + faceType);

                //The (1 byte) Image Data Type field denotes the encoding type of the Image Data block
                //JPEG 00 HEX
                //JPEG2000 lossy 01 HEX
                //JPEG 2000 lossless 02 HEX
                //PNG 03 HEX
                //Reserved by SC 37 for future use 04 HEX to FF HEX
                byte imageDataType = rdin.readByte();
                logger.debug("imageDataType >>>>>>>>>" + imageDataType);

                byte[] otherImageInformation = new byte[9];
                rdin.read(otherImageInformation, 0, otherImageInformation.length);

                //reading representationData -> imageData + 3d info + 3d data
                int lengthOfImageData = rdin.readInt();
                logger.debug("lengthOfImageData >>>>>>>>>" + lengthOfImageData);

                byte[] image = new byte[lengthOfImageData];
                rdin.read(image, 0, lengthOfImageData);

                extracts.add(new ExtractDTO(image, "face", imageDataType == 0 ? "jpg" :
                        imageDataType > 0 && imageDataType < 3 ? "jp2" : "png", 1));
            }
        } catch (Exception ex) {
            logger.error("Error extracting face image from biovalue", ex);
        }
        return extracts;
    }
}

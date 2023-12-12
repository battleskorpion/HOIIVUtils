package com.HOIIVUtils.hoi4utils.util;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;

import com.HOIIVUtils.hoi4utils.HOIIVFile;
import com.HOIIVUtils.hoi4utils.HOIIVUtils;

public class AATUpdateUtils {

    public static void main(String args[]) {
        // test for juddr

        HOIIVUtils.main(null);
        fixconsumergoodsfactor();
    }

    static double MULTIPLIER = 3.00;
	public static void fixconsumergoodsfactor() {
        File ideadirectory = HOIIVFile.ideas_folder ;
        for (File ideaFile: ideadirectory.listFiles()) {
            if (ideaFile.isDirectory()) {
                continue;
            }

//            Parser parser = new Parser ( ideaFile );
//            Expression[] exp = parser.expression().getAllSubexpressions ( "consumer_goods_factor=" );
//            for ( Expression simon:exp ) {
//               double v = simon.getDoubleValue() ;
//                v *= MULTIPLIER;
//            }

            Scanner s;
            try {
                s = new Scanner(ideaFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            StringBuilder fileBuffer = new StringBuilder();
            // todo was old deprecated parser
//            while (s.hasNextLine()) {
//                String line = s.nextLine();
//                System.out.println(line);
//                Expression exp = new Expression(line);
//                if (exp.contains("consumer_goods_factor=")) {
//                    double v = exp.getDoubleValue() * MULTIPLIER;
//                    NumberFormat nf = DecimalFormat.getInstance();
//                    nf.setMaximumFractionDigits(3);
//                    exp.setValue(Double.parseDouble(nf.format(v)));
//                    fileBuffer.append(exp.expression());
//                }
//                else {
//                    fileBuffer.append(line);
//                }
//
//                fileBuffer.append("\n");
//            }

            s.close();
            FileWriter writer = null;
            try {
                writer = new FileWriter(ideaFile, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedWriter bWriter = new BufferedWriter(writer);
            PrintWriter pWriter = new PrintWriter(bWriter);
            pWriter.print(fileBuffer);
            pWriter.close();

        }
    }
}

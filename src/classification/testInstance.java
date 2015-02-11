package classification;


import weka.classifiers.Classifier;
import weka.core.*;


/**
 * Created by Sudheera on 04/10/2014.
 */
public class testInstance {


    final static String trainingDataFileLocation = "dataFiles/train.txt";

    public String getClassifiedDA(String input) throws Exception {


        // deserializer model
        Classifier cls = (Classifier) weka.core.SerializationHelper.read("dataFiles/cls.model");

            MakeTestingInstance makeTestInst = new MakeTestingInstance();
            Instances testIns = makeTestInst.getTestingSet(trainingDataFileLocation, input);

            double pred = cls.classifyInstance(testIns.instance(0));
            System.out.println("predicted: " + testIns.classAttribute().value((int) pred));


        return testIns.classAttribute().value((int) pred);
    }
}
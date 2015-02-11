package classificationInterface;

import classification.testInstance;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


public class Controller {

    public TextField utteranceToClassify;
    public Label DALabel;
    public Label fileNameLabel;
    public ProgressBar progressBar;
    public Label percentageForprogressBar;

    //CheckBoxes for Dialogue Acts
    public CheckBox Reject;
    public CheckBox Apology;
    public CheckBox ConventionalClosing;
    public CheckBox Request_Command_Order;
    public CheckBox NoAnswer;
    public CheckBox YesAnswers;
    public CheckBox Backchannel;
    public CheckBox Abandoned;
    public CheckBox OpenQuestion;
    public CheckBox ConventionalOpening;
    public CheckBox Expressive;
    public CheckBox YesNoQuestion;
    public CheckBox Opinion;
    public CheckBox Statement;
    public CheckBox Thanking;
    public CheckBox BackchannelQuestion;

    //Buttons for actions
    public Button DownloadBtn;
    public Button classifyBtn;
    public Button selectAllBtn;
    public Button viewresultsBtn;

    //tab Panel
    public TabPane tabPane1;

    //Checkboxes for QA tab
    public CheckBox NoAnswerQA;
    public CheckBox YesAnswersQA;
    public CheckBox BackchannelQA;
    public CheckBox OpenQuestionQA;
    public CheckBox YesNoQuestionQA;
    public CheckBox BackchannelQuestionQA;

    public Button classifyBtnQA;
    public ProgressBar progressBarQA;
    public Label percentageQA;
    public Button DownloadBtnQA;
    public Button viewresultsBtnQA;
    public Label fileNameLabelQA;

    private boolean allSelected=false;

    File fileForClassify;
    File fileForClassifyQA;
    File downloadFile;
    File downloadFileQA;

    Hashtable<String,ArrayList<String>> table;
    ArrayList<String> questions;
    ArrayList<String> answers;

    public void classify(ActionEvent actionEvent) throws Exception {

        //get the utterance from the text field and Classify it
        String out = classifyInstance(utteranceToClassify.getCharacters().toString());
        System.out.println(out);

        //Printing to the Label
        DALabel.textProperty().setValue(out);
    }

    private String classifyInstance(String in) throws Exception {
        //using the testInstance class for classifying a single utterance
        classification.testInstance test = new testInstance();
        return test.getClassifiedDA(in);

    }


    public void openfile(ActionEvent actionEvent) {
        //initiate a new file chooser for choose a file
        final FileChooser fileChooser = new FileChooser();
        Node source = (Node)  actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        //assign the chosen file to classification
        fileForClassify = fileChooser.showOpenDialog(stage);
        //check if valid file chosen and enable the classify button
        if (fileForClassify != null) {
            fileNameLabel.textProperty().setValue(fileForClassify.getPath());
            classifyBtn.setDisable(false);
        }else{
            fileNameLabel.textProperty().setValue("No File Selected");
            classifyBtn.setDisable(true);
        }
    }

    public void openfileQA(ActionEvent actionEvent) {
        //initiate a new file chooser for choose a file
        final FileChooser fileChooser = new FileChooser();
        Node source = (Node)  actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        //assign the chosen file to classification
        fileForClassifyQA = fileChooser.showOpenDialog(stage);
        //check if valid file chosen and enable the classify button
        if (fileForClassifyQA != null) {
            fileNameLabelQA.textProperty().setValue(fileForClassifyQA.getPath());
            classifyBtnQA.setDisable(false);
        }else{
            fileNameLabelQA.textProperty().setValue("No File Selected");
            classifyBtnQA.setDisable(true);
        }
    }

    public void classifyFile(ActionEvent actionEvent) throws Exception {


        //create the classification task as a thread running separately
        final Task<Void> task = new Task<Void>() {
            public Void call() throws Exception {
                BufferedReader br1 = new BufferedReader(new FileReader(fileForClassify));
                String line=" ";

                //read the line numbers of the file
                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileForClassify));
                lineNumberReader.skip(Long.MAX_VALUE);
                int numLines = lineNumberReader.getLineNumber();
                int readLength = 0;
                //create a hashtable to store the classified utterances
                table = new Hashtable<String, ArrayList<String>>();

                //going through the line by line and classify
                while((line = br1.readLine()) != null){
                    System.out.println(line);
                    readLength += 1;

                    String s = classifyInstance(line);

                    //add the utterance to table if exists
                    if(table.containsKey(s)){
                        ArrayList<String> temp = table.get(s);
                        temp.add(line);
                        table.put(s,temp);
                        
                    }else{
                        //create new Arraylist if not exist already
                        ArrayList<String> temp = new ArrayList<String>();
                        temp.add(s);
                        temp.add(line);
                        table.put(s,temp);
                    }
                    
                    
                    //updating the progressbar measurements
                    final int done=readLength*100/numLines;
                    updateProgress(readLength,numLines);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            int done_perc=done;
                            if(done>=100){
                                done_perc=100;
                            }
                            percentageForprogressBar.textProperty().setValue(done_perc+"%");
                            if(done_perc==100){
                                DownloadBtn.setDisable(false);
                            }
                        }
                    });

                }
                return null;
            }
        };

        //Bind the properties with progress bar and starting the thread
        progressBar.progressProperty().bind(task.progressProperty());
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {

        //Choose an file location to download the file
        final FileChooser fileChooser = new FileChooser();
        Node source = (Node)  actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        downloadFile= fileChooser.showSaveDialog(stage);


                BufferedWriter bw = new BufferedWriter(new FileWriter(downloadFile));

        //iterating through each key checking that in the checkBoxes thoese are cheked.
        //if not continue;
        //else go ahead and print the arrayList
                for(Map.Entry<String, ArrayList<String>> entry : table.entrySet()) {

                    String key = entry.getKey();
                    ArrayList<String> list = entry.getValue();

                    if (key.equals("Statement") && !Statement.isSelected()) {
                        continue;
                    }else if (key.equals("Request/Command/Order")&& !Request_Command_Order.isSelected()) {
                        continue;
                    }else if (key.equals("Abandoned/Uninterpretable/Other") && !Abandoned.isSelected()) {
                        continue;
                    }else if (key.equals("Open Question")&& !OpenQuestion.isSelected()) {
                        continue;
                    }else if (key.equals("Yes-No Question")&& !YesNoQuestion.isSelected()) {
                        continue;
                    }else if (key.equals("Back-channel/Acknowledge")&& !Backchannel.isSelected()) {
                        continue;
                    }else if (key.equals("Opinion")&& !Opinion.isSelected()) {
                        continue;
                    }else if (key.equals("Thanking")&& !Thanking.isSelected()) {
                        continue;
                    }else if (key.equals("No Answer")&& !NoAnswer.isSelected()) {
                        continue;
                    }else if (key.equals("Expressive")&& !Expressive.isSelected()) {
                        continue;
                    }else if (key.equals("Yes Answers")&& !YesAnswers.isSelected()) {
                        continue;
                    }else if (key.equals("Conventional Closing")&& !ConventionalClosing.isSelected()) {
                        continue;
                    }else if (key.equals("Reject")&& !Reject.isSelected()) {
                        continue;
                    }else if (key.equals("Apology")&& !Apology.isSelected()) {
                        continue;
                    }else if (key.equals("Conventional Opening")&& !ConventionalOpening.isSelected()) {
                        continue;
                    }else if (key.equals("Backchannel Question")&& !BackchannelQuestion.isSelected()) {
                        continue;
                    }

                    bw.write(list.get(0));
                    bw.newLine();
                    bw.newLine();
                    bw.flush();

                    for (int i = 1; i < list.size(); i++) {
                        bw.write(list.get(i));
                        bw.newLine();
                        bw.flush();
                    }

                    bw.newLine();
                    bw.newLine();
                    bw.flush();


                }

                bw.close();

        //enable the view results button
        viewresultsBtn.setDisable(false);

        final Stage dialog = new Stage();
        Parent dia_root = FXMLLoader.load(getClass().getResource("dialog_box.fxml"));
        dialog.setTitle("File Download Complete");
        dialog.setScene(new Scene(dia_root, 250, 150));
        dialog.show();

    }

    public void viewResults(ActionEvent actionEvent) throws IOException {

        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.indexOf("win") >= 0){
            Runtime.getRuntime().exec("notepad "+downloadFile);
        }else{
            Runtime.getRuntime().exec("gedit "+downloadFile);
        }


        
    }

    public void exitDialogueBox(ActionEvent actionEvent) {
        Node source = (Node)  actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();

    }



    public void selectAllNone(ActionEvent actionEvent) {
        if (allSelected){
            Reject.setSelected(false);
            Apology.setSelected(false);
            ConventionalClosing.setSelected(false);
            Request_Command_Order.setSelected(false);
            NoAnswer.setSelected(false);
            YesAnswers.setSelected(false);
            Backchannel.setSelected(false);
            Abandoned.setSelected(false);
            OpenQuestion.setSelected(false);
            ConventionalOpening.setSelected(false);
            Expressive.setSelected(false);
            YesNoQuestion.setSelected(false);
            Opinion.setSelected(false);
            Statement.setSelected(false);
            Thanking.setSelected(false);
            BackchannelQuestion.setSelected(false);
            allSelected=false;
            selectAllBtn.textProperty().setValue("Select All");
        }else{
            Reject.setSelected(true);
            Apology.setSelected(true);
            ConventionalClosing.setSelected(true);
            Request_Command_Order.setSelected(true);
            NoAnswer.setSelected(true);
            YesAnswers.setSelected(true);
            Backchannel.setSelected(true);
            Abandoned.setSelected(true);
            OpenQuestion.setSelected(true);
            ConventionalOpening.setSelected(true);
            Expressive.setSelected(true);
            YesNoQuestion.setSelected(true);
            Opinion.setSelected(true);
            Statement.setSelected(true);
            Thanking.setSelected(true);
            BackchannelQuestion.setSelected(true);

            allSelected=true;
            selectAllBtn.textProperty().setValue("Select None");

        }

    }

    public void focusClassifyUtterance(ActionEvent actionEvent) {
        tabPane1.getSelectionModel().select(1);

    }

    public void focusClassifyMeeting(ActionEvent actionEvent) {
        tabPane1.getSelectionModel().select(2);

    }

    public void backToWelcome(ActionEvent actionEvent) {
        tabPane1.getSelectionModel().select(0);
    }

    public void focusClassifyQA(ActionEvent actionEvent) {

        tabPane1.getSelectionModel().select(3);
    }

    public void classifyQAPairs(ActionEvent actionEvent) {


        final Task<Void> task = new Task<Void>() {
            public Void call() throws Exception {
                BufferedReader br1 = new BufferedReader(new FileReader(fileForClassifyQA));
                String line=" ";

                LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileForClassifyQA));
                lineNumberReader.skip(Long.MAX_VALUE);
                int numLines = lineNumberReader.getLineNumber();
                int readLength = 0;
                questions = new ArrayList<String>();
                answers = new ArrayList<String>();

                int qaCount=0;

                while((line = br1.readLine()) != null){
                    System.out.println(line);
                    readLength += 1;

                    String s = classifyInstance(line);

                    if ((s.equals("Open Question") && OpenQuestionQA.isSelected())||(s.equals("Yes-No Question") && YesNoQuestionQA.isSelected())||(s.equals("Backchannel Question") && BackchannelQuestionQA.isSelected())){

                        questions.add(qaCount,line);
                    }else if((s.equals("Yes Answers") && YesAnswersQA.isSelected())||(s.equals("No Answer") && NoAnswerQA.isSelected())||(s.equals("Back-channel/Acknowledge") && BackchannelQA.isSelected())){

                        answers.add(qaCount,line);
                        qaCount++;
                    }

                    final int done=readLength*100/numLines;
                    updateProgress(readLength,numLines);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            int done_perc=done;
                            if(done>=100){
                                done_perc=100;
                            }
                            percentageQA.textProperty().setValue(done_perc+"%");
                            if(done_perc==100){
                                DownloadBtnQA.setDisable(false);
                            }
                        }
                    });

                }
                return null;
            }
        };

        progressBarQA.progressProperty().bind(task.progressProperty());
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();



    }

    public void downloadQAPairs(ActionEvent actionEvent) throws IOException {


        final FileChooser fileChooser = new FileChooser();
        Node source = (Node)  actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        downloadFileQA= fileChooser.showSaveDialog(stage);


        BufferedWriter bw = new BufferedWriter(new FileWriter(downloadFileQA));

        for (int i = 0; i < answers.size(); i++) {

            if(questions.size()<=i) {
             break;
            }

            bw.write("Q : "+questions.get(i));
            bw.newLine();
            bw.write("A : "+answers.get(i));
            bw.newLine();
            bw.newLine();
            bw.flush();



        }

            bw.newLine();
            bw.newLine();
            bw.flush();


        bw.close();

        viewresultsBtnQA.setDisable(false);

        final Stage dialog = new Stage();
        Parent dia_root = FXMLLoader.load(getClass().getResource("dialog_box.fxml"));
        dialog.setTitle("File Download Complete");
        dialog.setScene(new Scene(dia_root, 250, 150));
        dialog.show();


    }

    public void viewResults1(ActionEvent actionEvent) throws IOException {

        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.indexOf("win") >= 0){
            Runtime.getRuntime().exec("notepad "+downloadFileQA);
        }else{
            Runtime.getRuntime().exec("gedit "+downloadFileQA);
        }
    }
}

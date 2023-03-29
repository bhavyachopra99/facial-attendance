package application;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.scene.control.ProgressIndicator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

import application.FaceDetector;
import application.Database;
import application.OCR;
import application.Database;

public class SampleController {

	//**********************************************************************************************
	//Mention The file location path where the face will be saved & retrieved
	
	public String filePath="./faces";
	
	
	//**********************************************************************************************
	@FXML
	private Button startCam;
	@FXML
	private Button stopBtn;
	@FXML
	private Button motionBtn;
	@FXML
	private Button shapeBtn;
	@FXML
	private Button gestureStop;
	@FXML
	private Button saveBtn;
	@FXML
	private Button capBtn;
	@FXML
	private Button recogniseBtn;
	@FXML
	private Button stopRecBtn;
	@FXML
	private ImageView frame;
	@FXML
	private ImageView motionView;
	@FXML
	private AnchorPane pdPane;
	@FXML
	private TitledPane dataPane;
	@FXML
	private TextField fname;
	@FXML
	private TextField lname;
	@FXML
	private TextField code;
	@FXML
	private TextField reg;
	@FXML
	private TextField sec;
	@FXML
	private TextField age;
	@FXML
	public ListView<String> logList;
	@FXML
	public ListView<String> output;
	@FXML
	public ProgressIndicator pb;
	@FXML
	public Label savedLabel;
	@FXML
	public Label warning;
	@FXML
	public Label title;
	@FXML
	public TilePane tile;
	@FXML
	public TextFlow ocr;
//**********************************************************************************************
	FaceDetector faceDetect = new FaceDetector();	//Creating Face detector object									
	ColoredObjectTracker cot = new ColoredObjectTracker(); //Creating Color Object Tracker object		
	Database database = new Database();		//Creating Database object

	ArrayList<String> user = new ArrayList<String>();
	ImageView imageView1;
	
	public static ObservableList<String> event = FXCollections.observableArrayList();
	public static ObservableList<String> outEvent = FXCollections.observableArrayList();

	public boolean enabled = false;
	public boolean isDBready = false;

	
	//**********************************************************************************************
	public void putOnLog(String data) {

		Instant now = Instant.now();

		String logs = now.toString() + ":\n" + data;

		event.add(logs);

		logList.setItems(event);

	}

	@FXML
	protected void startCamera() throws SQLException {

		//*******************************************************************************************
		//initializing objects from start camera button event
		faceDetect.init();

		faceDetect.setFrame(frame);

		faceDetect.start();

		if (!database.init()) {

			putOnLog("Error: Database Connection Failed ! ");

		} else {
			isDBready = true;
			putOnLog("Success: Database Connection Succesful ! ");
		}

		//*******************************************************************************************
		//Activating other buttons
		startCam.setVisible(false);
		stopBtn.setVisible(true);
		capBtn.setDisable(false);
		motionBtn.setDisable(false);
		saveBtn.setDisable(false);

		if (isDBready) {
			recogniseBtn.setDisable(false);
		}

		dataPane.setDisable(false);

		if (stopRecBtn.isDisable()) {
			stopRecBtn.setDisable(false);
		}
		//*******************************************************************************************
		
		
		tile.setPadding(new Insets(15, 15, 55, 15));
		tile.setHgap(30);
		String path = filePath;

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		
		for (final File file : listOfFiles) {

			imageView1 = createImageView(file);
			tile.getChildren().addAll(imageView1);
		}
		putOnLog(" Real Time WebCam Stream Started !");
		
	}
	int count = 0;

	@FXML
	protected void faceRecognise() {

		
		faceDetect.setIsRecFace(true);
		recogniseBtn.setText("Get Face Data");
		user = faceDetect.getOutput();
		if (count > 0) {
			String t = "********* Face Data: " + user.get(1) + " " + user.get(2) + " *********";
			outEvent.add(t);
			String n1 = "First Name\t\t:\t" + user.get(1);
			outEvent.add(n1);
			output.setItems(outEvent);
			String n2 = "Last Name\t\t:\t" + user.get(2);
			outEvent.add(n2);
			output.setItems(outEvent);
			String fc = "Face Code\t\t:\t" + user.get(0);
			outEvent.add(fc);
			output.setItems(outEvent);
			String r = "Reg no\t\t\t:\t" + user.get(3);
			outEvent.add(r);
			output.setItems(outEvent);
			String a = "Age \t\t\t\t:\t" + user.get(4);
			outEvent.add(a);
			output.setItems(outEvent);
			String s = "Section\t\t\t:\t" + user.get(5);
			outEvent.add(s);
			output.setItems(outEvent);
		}

		count++;

		putOnLog("Face Recognition Activated !");

		stopRecBtn.setDisable(false);

	}

	@FXML
	protected void stopRecognise() {

		faceDetect.setIsRecFace(false);
		faceDetect.clearOutput();

		this.user.clear();

		recogniseBtn.setText("Recognise Face");

		stopRecBtn.setDisable(true);

		putOnLog("Face Recognition Deactivated !");

	}

	@FXML
	protected void startMotion() {

		faceDetect.setMotion(true);
		putOnLog("motion Detector Activated !");

	}

	@FXML
	protected void saveFace() throws SQLException {

		//Input Validation
		if (fname.getText().trim().isEmpty() || reg.getText().trim().isEmpty() || code.getText().trim().isEmpty()) {

			new Thread(() -> {

				try {
					warning.setVisible(true);

					Thread.sleep(2000);

					warning.setVisible(false);

				} catch (InterruptedException ex) {
				}

			}).start();

		} else {
			pb.setVisible(true);

			savedLabel.setVisible(true);

			new Thread(() -> {

				try {

					faceDetect.setFname(fname.getText());

					faceDetect.setFname(fname.getText());
					faceDetect.setLname(lname.getText());
					faceDetect.setAge(Integer.parseInt(age.getText()));
					faceDetect.setCode(Integer.parseInt(code.getText()));
					faceDetect.setSec(sec.getText());
					faceDetect.setReg(Integer.parseInt(reg.getText()));

					database.setFname(fname.getText());
					database.setLname(lname.getText());
					database.setAge(Integer.parseInt(age.getText()));
					database.setCode(Integer.parseInt(code.getText()));
					database.setSec(sec.getText());
					database.setReg(Integer.parseInt(reg.getText()));

					database.insert();
					
					javafx.application.Platform.runLater(new Runnable(){
						
						@Override
						 public void run() {
							pb.setProgress(100);
						 }
						 });


					

					savedLabel.setVisible(true);
					Thread.sleep(2000);
					
					javafx.application.Platform.runLater(new Runnable(){
						
						@Override
						 public void run() {
							pb.setVisible(false);
						 }
						 });

				
					
					

					
					
					javafx.application.Platform.runLater(new Runnable(){
						
						@Override
						 public void run() {
					 savedLabel.setVisible(false);
						 }
						 });

				} catch (InterruptedException ex) {
				}

			}).start();

			faceDetect.setSaveFace(true);

		}

	}

	@FXML
	protected void stopCam() throws SQLException {

		faceDetect.stop();

		startCam.setVisible(true);
		stopBtn.setVisible(false);

		/* this.saveFace=true; */

		putOnLog("Cam Stream Stopped!");

		recogniseBtn.setDisable(true);
		saveBtn.setDisable(true);
		dataPane.setDisable(true);
		stopRecBtn.setDisable(true);
		
		database.db_close();
		putOnLog("Database Connection Closed");
		isDBready=false;
	}


	@FXML
	protected void shapeStart() {

		// faceDetect.stop();

		SquareDetector shapeFrame = new SquareDetector();
		shapeFrame.loop();

	}

	private ImageView createImageView(final File imageFile) {

		try {
			final Image img = new Image(new FileInputStream(imageFile), 120, 0, true, true);
			imageView1 = new ImageView(img);

			imageView1.setStyle("-fx-background-color: BLACK");
			imageView1.setFitHeight(120);

			imageView1.setPreserveRatio(true);
			imageView1.setSmooth(true);
			imageView1.setCache(true);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return imageView1;
	}

}

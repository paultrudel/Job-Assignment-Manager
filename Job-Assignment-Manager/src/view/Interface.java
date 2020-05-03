package view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.controlsfx.control.CheckListView;

import jam.JobAssignmentManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Company;
import model.Job;
import model.Worker;

public class Interface extends Application {

	private static final int MARGIN = 20;
	public static final int FIT = 1200;
	public static final int MAP_WIDTH = FIT;
	public static final int MAP_HEIGHT = (int)(FIT / 1.42);
	
	private Scene scene;
	private Stage primaryStage;
	private BorderPane root;
	private Bounds bounds;
	private Group map;
	
	private Company company;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		company = new Company(this);
		draw();
	}

	private void draw() {
		root = new BorderPane();
		root.setPadding(new Insets(10, 20, 10, 20));
		drawButtons();
		drawMap();
		setScene();
		setStage();
	}
	
	private void drawButtons() {
		HBox buttonPane = new HBox(50);
		
		Button createJobs = new Button("Create Jobs");
		createJobs.setOnAction(e -> {
			createJobsDialog();
		});
		
		Button viewJobs = new Button("View Jobs");
		viewJobs.setOnAction(e -> {
			drawJobs();
			viewJobsDialog();
		});
		
		Button createWorkers = new Button("Create Workers");
		createWorkers.setOnAction(e -> {
			createWorkersDialog();
		});
		
		Button viewWorkers = new Button("View Workers");
		viewWorkers.setOnAction(e -> {
			viewWorkersDialog();
		});
		
		Button setIterations = new Button("Set Iterations");
		setIterations.setOnAction(e -> {
			setIterationsDialog();
		});
		
		Button generateAssignments = new Button("Generate Job Assignments");
		generateAssignments.setOnAction(e -> {
			company.generateJobAssignments();
		});
		
		Button viewAssignments = new Button("View Job Assignments");
		viewAssignments.setOnAction(e -> {
			viewAssignmentsDialog();
		});
		
		Button viewUtilities = new Button("View Utilities");
		viewUtilities.setOnAction(e -> {
			viewUtilitiesDialog();
		});
		
		Button reset = new Button("Reset");
		reset.setOnAction(e -> {
			company.reset();
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Reset");
			alert.setHeaderText(null);
			alert.setContentText("Jobs and employees have been reset");
			alert.showAndWait();
			draw();
		});
		
		buttonPane.getChildren().addAll(createJobs, viewJobs, createWorkers, viewWorkers, setIterations,
				generateAssignments, viewAssignments, viewUtilities, reset);
		buttonPane.setAlignment(Pos.CENTER);
		root.setBottom(buttonPane);
		BorderPane.setAlignment(buttonPane, Pos.CENTER);
	}
	
	private void createJobsDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Create Jobs");
		dialog.setHeaderText("Create jobs, add a job, or reset all jobs");
		
		ButtonType reset = new ButtonType("Reset Jobs", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(reset, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		grid.add(new Label("Create a random set of jobs"), 0, 0);
		TextField numJobs = new TextField();
		
		Button create = new Button("Create Random Jobs");
		create.setOnAction(e -> {
			company.createJobs(Integer.parseInt(numJobs.getText()));
		});
		
		grid.add(new Label("Number of jobs:"), 0, 1);
		grid.add(numJobs, 1, 1);
		grid.add(create, 0, 2);
		
		grid.add(new Label("Add a job"), 0, 3);
		
		ComboBox<Integer> type = new ComboBox<Integer>();
		for(Integer i: Company.JOB_TYPES)
			type.getItems().add(i);
		
		ComboBox<Integer> duration = new ComboBox<Integer>();
		for(Integer i: Company.JOB_DURATIONS)
			duration.getItems().add(i);
		
		TextField location = new TextField();
		location.setPromptText("x,y");
		
		Button add = new Button("Add Job");
		add.setOnAction(e -> {
			int t = type.getSelectionModel().getSelectedItem();
			int d = duration.getSelectionModel().getSelectedItem();
			String[] coords = location.getText().split(",");
			double x = Double.parseDouble(coords[0]);
			double y = Double.parseDouble(coords[1]);
			company.addJob(new Job(t, d, x, y));
			type.getSelectionModel().clearSelection();
			duration.getSelectionModel().clearSelection();
			location.setText("");
		});
		
		grid.add(new Label("Job type:"), 0, 4);
		grid.add(type, 1, 4);
		grid.add(new Label("Job duration (in minutes):"), 0, 5);
		grid.add(duration, 1, 5);
		grid.add(new Label("Job location:"), 0, 6);
		grid.add(location, 1, 6);
		grid.add(add, 0, 7);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == reset) {
				company.resetJobs();
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void viewJobsDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Jobs");
		dialog.setHeaderText("Current jobs in the system");
		
		ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		TableView<Job> table = new TableView<Job>();
		ObservableList<Job> jobs = FXCollections.observableArrayList(company.getJobs().values());
		
		TableColumn<Job, String> idCol = new TableColumn<Job, String>("Job Number");
		idCol.setCellValueFactory(new PropertyValueFactory<>("number"));
		
		TableColumn<Job, String> typeCol = new TableColumn<Job, String>("Job Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("typeProperty"));
		
		TableColumn<Job, String> durationCol = new TableColumn<Job, String>("Job Duration");
		durationCol.setCellValueFactory(new PropertyValueFactory<>("durationProperty"));
		
		TableColumn<Job, String> locationCol = new TableColumn<Job, String>("Job Location");
		locationCol.setCellValueFactory(new PropertyValueFactory<>("locationProperty"));
		
		table.setItems(jobs);
		table.getColumns().addAll(idCol, typeCol, durationCol, locationCol);
		
		grid.add(table, 0, 0);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == confirm) {
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void createWorkersDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Create Workers");
		dialog.setHeaderText("Create workers, add a worker, or reset all workers");
		
		ButtonType reset = new ButtonType("Reset Workers", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(reset, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		grid.add(new Label("Create a random set of Workers"), 0, 0);
		TextField numWorkers = new TextField();
		
		Button create = new Button("Create Random workers");
		create.setOnAction(e -> {
			company.createWorkers(Integer.parseInt(numWorkers.getText()));
		});
		
		grid.add(new Label("Number of workers:"), 0, 1);
		grid.add(numWorkers, 1, 1);
		grid.add(create, 0, 2);
		
		grid.add(new Label("Add a worker"), 0, 3);
		ObservableList<Integer> types = FXCollections.observableArrayList();
		for(Integer i: Company.JOB_TYPES)
			types.add(i);
		CheckListView<Integer> skills = new CheckListView<Integer>(types);
		skills.prefHeightProperty().bind(Bindings.size(types).multiply(30));
		
		Button add = new Button("Add Worker");
		add.setOnAction(e -> {
			List<Integer> skillsList = new ArrayList<>(skills.getSelectionModel().getSelectedItems());
			int[] skillsArray = new int[skillsList.size()];
			for(int i = 0; i < skillsArray.length; i++) {
				int s = skillsList.get(i).intValue();
				skillsArray[i] = s;
			}
			company.addWorker(new Worker(skillsArray));
		});
		
		grid.add(new Label("Worker skill set:"), 0, 4);
		grid.add(skills, 1, 4);
		grid.add(new Label("Job duration:"), 0, 5);
		grid.add(add, 0, 5);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == reset) {
				company.resetWorkers();
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void viewWorkersDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Workers");
		dialog.setHeaderText("Current workers in the system");
		
		ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		TableView<Worker> table = new TableView<Worker>();
		ObservableList<Worker> workers = FXCollections.observableArrayList(company.getWorkers().values());
		table.setItems(workers);
		
		TableColumn<Worker, String> idCol = new TableColumn<Worker, String>("Worker Number");
		idCol.setCellValueFactory(new PropertyValueFactory<>("number"));
		
		TableColumn<Worker, String> skillsCol = new TableColumn<Worker, String>("Worker Skills");
		skillsCol.setCellValueFactory(new PropertyValueFactory<>("skillsProperty"));
		
		table.getColumns().addAll(idCol, skillsCol);
		
		grid.add(table, 0, 0);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == confirm) {
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void setIterationsDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Set Iterations");
		dialog.setHeaderText("Set the number of iterations that the algorithm should perform");
		
		ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm, ButtonType.CANCEL);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		grid.add(new Label("# of iterations"), 0, 0);
		TextField numIterations = new TextField();
		numIterations.setText(String.valueOf(JobAssignmentManager.getMaxIterations()));
		grid.add(numIterations, 1, 0);
		
		grid.add(new Label("Increment size"), 0, 1);
		TextField incrementSize = new TextField();
		incrementSize.setText(String.valueOf(JobAssignmentManager.getIncrements()));
		incrementSize.setEditable(false);
		grid.add(incrementSize, 1, 1);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == confirm) {
				JobAssignmentManager.setMaxIterations(Integer.parseInt(numIterations.getText()));
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void viewAssignmentsDialog() {
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Job Assignments");
		dialog.setHeaderText("Final worker job assignments");
		
		ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		grid.add(new Label("Profit from this job assignment: " + company.getProfit()), 0, 0);
		
		TableView<Worker> table = new TableView<Worker>();
		ObservableList<Worker> workers = FXCollections.observableArrayList(company.getWorkers().values());
		table.setItems(workers);
		
		TableColumn<Worker, String> idCol = new TableColumn<Worker, String>("Worker Number");
		idCol.setCellValueFactory(new PropertyValueFactory<>("number"));
		
		TableColumn<Worker, String> jobsCol = new TableColumn<Worker, String>("Assigned Jobs");
		jobsCol.setCellValueFactory(new PropertyValueFactory<>("jobsProperty"));
		
		table.getColumns().addAll(idCol, jobsCol);
		
		table.setRowFactory(rf -> {
			TableRow<Worker> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
					Worker worker = row.getItem();
					drawJobs(worker);
				}
			});
			return row;
		});
		
		grid.add(table, 0, 1);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == confirm) {
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void viewUtilitiesDialog() {
		List<Double> epochs = new ArrayList<Double>();
		List<Double> utilities = new ArrayList<Double>();
		for(Map.Entry<Integer, Double> entry: company.getUtilities().entrySet()) {
			epochs.add((double) entry.getKey());
			utilities.add(entry.getValue());
		}
		
		double minUtility = Collections.min(utilities);
		double maxUtility = Collections.max(utilities);
		Plot plot = Plot.plot(Plot.plotOpts().title("Utilities Over Iterations"));
		plot.xAxis("Epochs", Plot.axisOpts().range(0, epochs.size()));
		plot.yAxis("Utility Values", Plot.axisOpts()
				.range(minUtility + (minUtility / 20.0), maxUtility + (maxUtility / 20.0)));
		plot.series("Data", Plot.data().xy(epochs, utilities), Plot.seriesOpts()
				.marker(Plot.Marker.DIAMOND)
				.markerColor(java.awt.Color.GREEN)
				.color(java.awt.Color.BLACK));
		String plotName = company.getNumJobs() + "Jobs " + company.getNumWorkers() + "Workers" + 
				JobAssignmentManager.getMaxIterations() + "Iterations";
		try {
			plot.save("plots" + "/" + plotName, "png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Dialog<?> dialog = new Dialog<>();
		dialog.setTitle("Utilities");
		dialog.setHeaderText("Utility value of job assignments over time");
		
		ButtonType confirm = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirm);
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		Image plotImg = new Image("file:plots/" + plotName + ".png");
		ImageView plotImgView = new ImageView(plotImg);
		grid.add(plotImgView, 0, 0);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.setResultConverter(button -> {
			if(button == confirm) {
				return null;
			}
			return null;
		});
		
		dialog.showAndWait();
	}
	
	private void drawMap() {
		Image img = new Image("file:images/map.jpg");
		ImageView imgView = new ImageView(img);
		imgView.setFitHeight(FIT);
		imgView.setFitWidth(FIT);
		imgView.setPreserveRatio(true);
		map = new Group(imgView);
		
		Point2D location = Company.LOCATION;
		Rectangle c = new Rectangle(location.getX(), location.getY(), 15,
				15);
		c.setFill(Color.GREEN);
		c.setStroke(Color.BLACK);
		map.getChildren().add(c);
		root.setCenter(map);
	}
	
	private void drawJobs() {
		ArrayList<Job> jobs = new ArrayList<>(company.getJobs().values());
		if(jobs.size() > 0 && jobs.size() <= 100) {
			for (Job job : jobs) {
				Point2D location = job.getLocation();
				Circle circle = new Circle(location.getX(), location.getY(), 5);
				circle.setFill(Color.BLUE);
				circle.setStroke(Color.BLACK);
				map.getChildren().add(circle);
			}
		}
	}
	
	private void drawJobs(Worker worker) {
		drawMap();
		ArrayList<Job> jobs = worker.getJobs();
		if(jobs.size() > 0  && jobs.size() < 100) {
			for(Job job: jobs) {
				Point2D location = job.getLocation();
				Circle circle = new Circle(location.getX(), location.getY(), 5);
				circle.setFill(Color.BLUE);
				circle.setStroke(Color.BLACK);
				map.getChildren().add(circle);
			}
		}
	}
	
	public void showAlert(String title, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	private void setScene() {
		bounds = root.getLayoutBounds();
		scene = new Scene(root);
	}
	
	private void setStage() {
		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
	    double factor = Math.min(visualBounds.getWidth() / (bounds.getWidth() + MARGIN),
	            visualBounds.getHeight() / (bounds.getHeight() + MARGIN));
	    primaryStage.setScene(scene);
	    primaryStage.setWidth((bounds.getWidth() + MARGIN) * factor);
	    primaryStage.setHeight((bounds.getHeight() + MARGIN) * factor);
	    primaryStage.setTitle("Job Assignment Manager");
	    primaryStage.setMaximized(true);
	    primaryStage.show();
	}
	
	public static void main(String args[]) {
		launch(args);
	}
}

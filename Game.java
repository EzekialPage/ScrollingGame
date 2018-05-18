package Game;

import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class Game extends ScoreManager
{	
  private Grid grid;					//holds grid for images
  private int userRow;					//row user is currently in
  private int userColumn;				//column user is currently in
  private int msElapsed;				//number of milliseconds elapsed
  private int timesGet;					//number of times collecting goal object
  private int timesAvoid;				//number of times hitting obstacles
  private LinkedList questionList = new LinkedList();
  private static Scores [] scores;
  private int correct = 0;
  
  String user = "birb3.png";
  String obstacle = "birb4.png";
  String target = "Stick3.png";
  
  //creates default game1 layout
  public Game()
  {
    grid = new Grid(9, 17, 1920);
    userRow = 4;
    userColumn = 0;
    msElapsed = 0;
    timesGet = 0;
    timesAvoid = 0;
    updateTitle();
    grid.setImage(new Location(userRow, userColumn), user);
    
    for(int x = 0; x < grid.getNumCols(); x++) {
    	for(int y = 0; y < grid.getNumRows(); y++) {
    		grid.setColor(new Location(y, x), new Color(135, 206, 250));
    	}
    }
  }
  
  //method to create question sets
  public void setQuestions() {
	  File inFile = new File("Questions.txt");
	  try {
		Scanner scan = new Scanner(inFile);
		while(scan.hasNext()) {
			String inLine = scan.nextLine();
			String [] line = inLine.split("_");
			int id = Integer.parseInt(line[0]);
			String question = line[1] ;
			String answer = line[2];
			
			Question q1 = new Question(id, question, answer);
			questionList.addTail(q1);
		}
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		JOptionPane.showMessageDialog(null, "No questions file");
	}
  }
  
  //method to display question
  public void showQuestion() {
	  Random rand = new Random();
	  int targetID = rand.nextInt(49);
	  
	  Question q1 = questionList.search(targetID);
	  String ans = JOptionPane.showInputDialog(null, q1.getQuestion());
	  if(ans.equalsIgnoreCase(q1.getAnswer())) {
		  JOptionPane.showMessageDialog(null, "Good Job!");
		  correct ++;
	  }else {
		  JOptionPane.showMessageDialog(null, "Incorrect");
		  timesAvoid++;
	  }
  }
  
  //Method to run the game
  public void play()
  {
	scores = getScores();
	setQuestions();
    while (!isGameOver())
    {
      handleKeyPress();
      if(msElapsed < 4000) {
    	  grid.pause(200);
      }else if(msElapsed < 10000) {
    	  grid.pause(150);
      }else {
    	  grid.pause(100);
      }
      
      if (msElapsed % 300 == 0)
      {
        scrollLeft();
      }
      if(msElapsed % 900 == 0){
    	populateRightEdge();
      }
      if(msElapsed % 10000 == 0 && msElapsed != 0) {
    	  showQuestion();
      }
      updateTitle();
      msElapsed += 100;
    }
    if(timesGet >= 15) {
    	JOptionPane.showMessageDialog(null, ""
    			+ "                 Game Over\n"
    			+ "Congratulations you completed the nest!\n"
    			+ "                Score: " + getScore());
    }else {
    	JOptionPane.showMessageDialog(null, ""
    			+ "             Game Over\n"
    			+ " Almost completed the nest.\n"
    			+ "            Score: " + getScore());
    }
  }
  
  //handles input from user
  public void handleKeyPress(){
	  int key = grid.checkLastKeyPressed();
	  handleCollision(new Location(userRow, userColumn));
	  grid.setImage(new Location(userRow, userColumn), null);
	  
	  //handle moving left
	  if(key == 37 && userColumn > 0) {
		  userColumn--;
		  
	  //handle moving up
  	  }else if(key == 38 && userRow > 0) {
		  userRow--;
		  
	  //handle moving right
	  }else if(key == 39 && userColumn < grid.getNumCols() - 1) {
		  userColumn++;
		  
	  //handle moving down
	  }else if(key == 40 && userRow < grid.getNumRows() - 1) {
		  userRow++;
	  }
	  handleCollision(new Location(userRow, userColumn));
	  grid.setImage(new Location(userRow, userColumn), user);
  }
  
  //randomly places obstacles and target objects at the right edge of the grid
  public void populateRightEdge(){
	  if(msElapsed <= 2400) {
		  place(obstacle);
		  place(target);
	  }else if(msElapsed <= 6000) {
		  place(obstacle);
		  place(obstacle);
		  place(target);
	  }else {
		  place(obstacle);
		  place(obstacle);
		  place(obstacle);
		  place(target);
	  }
  }
  
  //helper method places specified img at random location
  public void place(String gif) {
	  Random rand = new Random();
	  int row = rand.nextInt(grid.getNumRows());
	    
	  //place obstacle
	  if(grid.getImage(new Location(row, grid.getNumCols() -1)) == null) {
	  	grid.setImage(new Location(row, grid.getNumCols() -1), gif);
	  }else{
		  place(gif);
	  }
  }
  
  //moves every image left 1 space
  public void scrollLeft(){
	  String img = null;
	  for(int col = 0; col < grid.getNumCols(); col++){
		  for(int row = 0; row < grid.getNumRows(); row++){
			  if(col == grid.getNumCols() -1){
				  grid.setImage(new Location(row, col), null);
			  }else{
				  img = grid.getImage(new Location(row, col + 1));
				  try{
					  if(!img.equals(user)){
						  grid.setImage(new Location(row, col), img);
					  }else {
						  grid.setImage(new Location(row, col), null);
					  }
				  }catch(Exception NullPointerException){
					  grid.setImage(new Location(row, col), null);
				  }
			  }
		  }
	  }
  }
  
  //deals with results of running into objects in game
  public void handleCollision(Location loc){
	  String img = grid.getImage(loc);
	  if(img != null && img.equals(target)) {
		  timesGet ++;
	  }else if(img != null && img.equals(obstacle)) {
		  timesAvoid ++;
	  }
	  
  }
  
  //returns users score
  public int getScore()
  {
    return (timesGet * 100) + (correct * 100);
  }
  
  //sets the grid title to
  public void updateTitle()
  {
    grid.setTitle("Sticks:  " + timesGet + "                    " + "Lives: " + (5 - timesAvoid));
  }
  
  public boolean isGameOver()
  {
	  if(timesAvoid < 5){
		  return false;
	  }else {
		  grid.closeWindow();
		  return true;
	  }
  }
  
  public static void showScores(Scores [] s) {
	  String out = "          High Scores\n";
	  for(int i = 0; i < s.length;i++) {
		  out += i + 1 + ":    " + s[i].toString() + "\n";
	  }
	  JOptionPane.showMessageDialog(null, out);
  }
  
	public static void storeScores(String s) throws IOException {
		try {
			
			BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter(new FileOutputStream("HighScores.txt"), StandardCharsets.UTF_8));
			bufferedWriter.write(s);
			bufferedWriter.flush();
			bufferedWriter.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("couldn't create buffered writer");
		}
	}
  
  public static void main(String[] args) throws IOException
  {
	  int playAgain = 0;
	  while(playAgain == 0) {
		  //ask for username
		  String name = JOptionPane.showInputDialog(null, "Enter your initials:");
		  
		  //tell story
		  JOptionPane.showMessageDialog(null, "The bird needs 15 sticks for to make her new nest. Help her collect sticks and avoid the other birds."
		  		+ "\nScore points by gathering sticks and answering math questions");
		  
		  //start game
		  Game game = new Game();
		  game.play();
		  
		  
		  Scores s1 = new Scores(name, game.getScore());
		  String s = setScores(s1);
		  storeScores(s);
		  showScores(getScores());
		  Object [] options = {"Yes", "No"};
		  playAgain = JOptionPane.showOptionDialog(null, "Would you like to play again?\n", "title", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		  //storeScores
		  //String [] names = {"???", "???", "???", "???", "???", "???", "???", "???", "???", "???"};
		  //int [] scores =  {000, 000, 000, 000, 000, 000, 000, 000, 000, 000};
	  }
	  
  }
}

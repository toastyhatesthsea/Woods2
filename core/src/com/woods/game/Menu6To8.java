package com.woods.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Selection screen for the students BETWEEN kindergarten and 6-8
 */
public class Menu6To8 implements Screen, Menu {

    Menu6To8 menu6To8;
    Group labelGroup, textFieldGroup, buttonSelectionGroup, imageGroup, warningGroup, backgroundGroup; //Button groups are used to store 'Actor' objects
    TextField rowTextField, colTextField; //Used for player input
    Button exitButton, startButton; //Used to draw buttons on the screen for players to press
    Label rowLabel, colLabel, gameInfoLabel; //Just basic text to be drawn on the screen

    final Woods game; //A reference to the main 'Game' class
    final MenuScreen aMenuScreen; //A reference to the 'Screen' that called this current screen
    ShapeRenderer aShape; //used for drawing basic Shapes
    BoardController aBoardController;
    MenuController aMenuController; //Used for gathering Menu assets, such as Buttons, Images...etc
    Skin someSkin; //A libgdx object to store 'Styles' <-----Styles are used for setting how Buttons/Textfields..etc will look like when drawn and used on the screen
    Stage uiStage, backgroundStage; //Stages are used for storing 'Actor' objects
    Board boardOfScreen; //Used for dimensions of screen
    Background rainDropsBackground; //Used for drawing backgrounds, such as the moving 'Raindrop' background
    float animationStateTime; //Used in conjunction with the background and animations, determines which 'texture' or sprite to draw at the current time
    int rows, columns, players; //Sets the amount of rows, columns..etc
    float inputTime; //Used to make sure when the ESC button is pressed, it isn't rapidly activated in succession when changing screens

    public Menu6To8(Woods game, MenuScreen aMenuScreen, int rows, int columns) {
        menu6To8 = this;
        this.rows = rows; //Default rows
        this.columns = columns; //Default columns
        this.game = game;
        this.aMenuScreen = aMenuScreen;
        aShape = new ShapeRenderer();
        someSkin = new Skin();
        uiStage = new Stage(game.aViewport);
        backgroundStage = new Stage(game.aViewport);
        boardOfScreen = new Board(50, 50, game.camera.viewportWidth / 50,
                game.camera.viewportHeight / 50);
        rainDropsBackground = new Background(game.backgroundTextures, 30, .05f, game.camera, 4, 4);
        animationStateTime = 0f;
        aMenuController = new MenuController(game, this);
        labelGroup = new Group();
        textFieldGroup = new Group();
        buttonSelectionGroup = new Group();
        backgroundGroup = new Group();
        warningGroup = new Group();
        addBackground();
        addButtons();
        addTextFields();
        addLabels();
        assembleMenu();
    }

    @Override
    public void addLabels() {
        rowLabel = aMenuController.getRowLabel();
        colLabel = aMenuController.getColumnLabel();
        gameInfoLabel = aMenuController.getSixToEightWelcomeLabel();
        labelGroup.addActor(rowLabel);
        labelGroup.addActor(colLabel);
        labelGroup.addActor(gameInfoLabel);
    }

    @Override
    public void addTextFields() {
        rowTextField = aMenuController.getRowTextField();
        colTextField = aMenuController.getColTextField();
        textFieldGroup.addActor(rowTextField);
        textFieldGroup.addActor(colTextField);
    }

    @Override
    public void addBackground() {
        backgroundGroup = aMenuController.getBackgroundTreeImageGroup();
    }

    @Override
    public void addButtons() {
        startButton = aMenuController.getStartButton();
        exitButton = aMenuController.getExitButton();
        buttonSelectionGroup.addActor(exitButton);
        buttonSelectionGroup.addActor(startButton);
    }

    @Override
    public void assembleMenu() {
        backgroundStage.addActor(backgroundGroup);
        uiStage.addActor(textFieldGroup);
        uiStage.addActor(buttonSelectionGroup);
        uiStage.addActor(labelGroup);
        uiStage.addActor(exitButton);
    }

    @Override
    public boolean removeLabels()
    {
        return false;
    }

    @Override
    public boolean removeListeners()
    {
        return false;
    }

    /**
     * Used for error checking when players are entering Digits into textfields on the screen
     * @param string String
     * @return boolean <- Checks whether data entered ARE digits or NOT
     */
    private boolean isInt(String string) { // assuming integer is in decimal number system
        if (string == "") //Null values are not ints
            return false;
        //Loops through each character and checks if they are digits
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Checks to make sure Board size aren't too large or small
     * @param side int <-- Row or Column size
     * @return boolean
     */
    private boolean isValidBoardSize(int side){
        if (side >= 2 && side <= 50){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addListeners() {

        /* Just gets text from a player and checks whether they are valid digits */
        rowTextField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                int tempRows = 0;
                if (isInt(textField.getText())){
                    tempRows = Integer.parseInt(textField.getText());
                }
                rows = tempRows;
            }
        });

        /* Just gets text from a player and checks whether they are valid digits */
        colTextField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                int tempColumns = 0;
                if (isInt(textField.getText())){
                    tempColumns = Integer.parseInt(textField.getText());
                }
                columns = tempColumns;
            }
        });

        /*Starts the actual gameplay if player input is valid */
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (isValidBoardSize(rows) && isValidBoardSize(columns)) {
                    inputTime = 0; //resets input time when screens are changed, so when THIS Object screen is set again there is a delay when pressing ESC
                    game.forestMusic.stop();
                    game.setScreen(new BoardScreen(game, menu6To8, rows, columns, true));
                    rowTextField.getStyle().fontColor = Color.WHITE;
                } else {
                    rowTextField.getStyle().fontColor = Color.RED;
                    game.invalidInput.play();
                }
            }
        });

        /* Exits the screen and goes back to the previous Screen when button is pressed */
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(aMenuScreen);
            }
        });

    }

    /**
     * Updates some states of the game. Mostly used for keyboard input
     * @param deltaTime float <-- Time since last rendered(drawn) frame
     */
    private void update(float deltaTime) {
        Input anInput = Gdx.input;
        inputTime += deltaTime;

        if (anInput.isKeyPressed(Input.Keys.ESCAPE))
        {
            //This will make sure that screens don't rapidly exit in succession if ESC key is pressed
            if (inputTime > 1f)
            {
                inputTime = 0; //resets input time when screens are changed, so when THIS Object screen is set again there is a delay when pressing ESC
                game.setScreen(aMenuScreen);
            }
        }

        if (aMenuController.getRows() < 2 || aMenuController.getRows() > 50) {
            warningGroup.addActor(aMenuController.getRowWarning());
        } else {
            warningGroup.removeActor(aMenuController.getRowWarning());
        }
    }

    /**
     * Everything in this function is called IMMEDIATELY AFTER the constructor for this class when this screen is activated.
     * A good place to add Listeners and Play music when starting this screen.
     */
    @Override
    public void show() {

        /*Sets the Stage that will accept input. Basically any actors(Buttons, Textfields..etc) that have 'listeners' in this stage will listen to input*/
        Gdx.input.setInputProcessor(uiStage);
        game.forestMusic.play(); //Just plays music immediately when this screen is activated
        addListeners();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0f, 0); //Just clears the screen to the specified color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //Clears buffer
        aShape.setProjectionMatrix(game.camera.combined);//Takes the camera width and height and tells the renderer how to project World-width and World-height into pixels
        animationStateTime += Gdx.graphics.getDeltaTime(); // getDeltaTime() is the amount of time since the last frame rendered

        rainDropsBackground.draw(game.batch, animationStateTime); //Draws the background according to the current texture frame. Texture frame is chosen based by animationStateTime
        backgroundStage.act(); //Updates all Actor objects that have been added to this stage. Uses the 'act(float delta)' function to update the Actor state
        backgroundStage.draw(); //Draws all Actor objects that have been added to this stage
        uiStage.act(); //All actors on the 'stage' are updated using their 'act(float delta)' internal functions
        uiStage.draw(); //Draws everything on the stage

        update(delta);
    }

    /**
     * This adjusts and scales the screen if it is resized. Otherwise will look out of place and too small/big
     * @param width  int
     * @param height int
     */
    @Override
    public void resize(int width, int height) {
        game.aViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}

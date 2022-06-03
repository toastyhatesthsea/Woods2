package com.woods.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;

//TODO Make Screen Controller
/**
 * This will implement a 'Board screen' for the actual gameplay
 */
public class BoardScreen implements Screen, Menu
{
    /**
     * This will help set the 'state' of the game. Whether it is running or paused or etc.
     */
    public enum State
    {
        PAUSE,
        RUN,
        RESUME,
        STOPPED,
        FOUND,
        PLACEMENT
    }

    BoardController aBoardController; //Used for controlling the gameplay board
    Woods game;
    ShapeRenderer aShape; //used for rendering basic shapes
    MenuController aMenuController; //for gathering gameplay assets. Buttons, textures..etc

    int rows, columns; //number of rows and columns to create for the gameplay board
    float inputTime; //Used to determine input time and to make sure input processing doesn't occur too fast
    Screen returnScreen; //Saving screen that called BoardScreen to easily return to the previous screen
    State stateOfGame;
    State beginningState; //The state that the game starts at
    boolean changeMovement = false;

    Stage uiStage; /*A Libgdx object that creates a 'stage' for other objects to be placed into, such as the
    libgdx actor objects or any objects that extend the Actor class*/
    int rightSideBuffer; //Used for leaving blank space on the right side of the board if desired
    int bottomEdgeBuffer; //Used for leaving blank space on the right side of the board if desired
    Player playerUp = null;

    Button resetButton; //A listener button that will reset the board
    Button exitButton; //A listener button that will exit the gamescreen
    Sprite aSprite;
    Skin someSkin;

    /*The following three functions are used to draw text on the sceen*/
    statistics statisticsFunc;
    found foundFunc;
    placement placementFunc;

    BitmapFont arrowKeyFont;
    Group collisionStars; //This group is used for storing an animation to be displayed when a collision occurs


    private BoardScreen(final Woods aGame, Screen returnScreen, final int rows, final int columns, State stateOfGame){
        this.arrowKeyFont = new BitmapFont(Gdx.files.internal("monospace.fnt"));

        this.returnScreen = returnScreen; //saves the screen that called the BoardScreen, so you can return to the previous screen easily
        this.game = aGame;
        this.rows = rows; //The amount of rows on the gameplay Screen
        this.columns = columns; //The amount of columns on the gameplay Screen
        this.aShape = new ShapeRenderer(); //A renderer for basic shapes, triangles, rectangle...etc
        this.uiStage = new Stage(aGame.aViewport);
        this.someSkin = new Skin();
        this.collisionStars = new Group();

        this.rightSideBuffer = 0;
        this.bottomEdgeBuffer = 0;

        //Subtracting the rightSideBuffer from theCamera.viewportWidth or height will leave blank space on the right side or bottom side
        aBoardController = new BoardController(aGame, rows, columns, (game.camera.viewportWidth - rightSideBuffer) / columns,
                (game.camera.viewportHeight - bottomEdgeBuffer) / rows);
        aBoardController.createArrayOfTextures(aGame.boardTextures); //Creates a gameplay Board with certain textures to be used as a background
        aMenuController = new MenuController(aGame, returnScreen);

        this.stateOfGame = stateOfGame;
        this.beginningState = stateOfGame;
        this.addButtons();

        this.foundFunc = new found()
        {
            @Override
            public void drawCollision(SpriteBatch aBatch)
            {
                game.medievalFont.setColor(Color.MAGENTA.r, Color.MAGENTA.g, Color.MAGENTA.b, 1);
                game.medievalFont.draw(game.batch, "Players found each other!", game.camera.viewportWidth / 2, game.camera.viewportHeight / 2, 20f, 1, true);
            }
        };

        this.statisticsFunc = new statistics()
        {
            @Override
            public void drawStatistics(SpriteBatch aBatch)
            {
                game.monoFont.setColor(1, 1, 0, 1.3f);
                game.medievalFont.draw(game.batch, "Total Moves -- " + aBoardController.totalPlayerMovements, 50, game.camera.viewportHeight - 10);
                game.medievalFont.setColor(Color.ORANGE);
                game.medievalFont.draw(game.batch, "Average: " + aBoardController.getAverage(), 375, game.camera.viewportHeight - 10);
                game.medievalFont.setColor(Color.GOLD);
                game.medievalFont.draw(game.batch, "Highest: " + aBoardController.highest, 90, game.camera.viewportHeight - 70);
                game.medievalFont.draw(game.batch, "Lowest: " + aBoardController.lowest, 110, game.camera.viewportHeight - 110);

                game.monoFont.draw(game.batch, "Rows: " + rows, game.camera.viewportWidth - 250, game.camera.viewportHeight - 10);
                game.monoFont.draw(game.batch, "Columns: " + columns, game.camera.viewportWidth - 250, game.camera.viewportHeight - 40);

            }
        };

        this.placementFunc = new placement()
        {
            @Override
            public void drawPlacementInstructions(SpriteBatch aBatch)
            {
                game.medievalFont.setColor(Color.MAGENTA.r, Color.MAGENTA.g, Color.MAGENTA.b, 1);
                game.medievalFont.draw(game.batch, "Click in a tile to add a player.  " +
                                "Click again to change the player's movement. After creating two or more players, press the Enter key to begin.",
                        10, game.camera.viewportHeight/2, game.camera.viewportWidth-20, 1, true);
            }
        };

    }


    public BoardScreen(Woods aGame, Screen returnScreen, int rows, int columns, boolean changeMovement ) {
        this(aGame, returnScreen,rows,columns, State.PLACEMENT);
        this.changeMovement = changeMovement;
    }


    public BoardScreen(Woods aGame, Screen returnScreen, int rows, int columns, int numPlayers)
    {
        this(aGame, returnScreen,rows,columns, State.RUN);
        aBoardController.createPlayersDefaultLocation(numPlayers);
    }

    @Override
    public void addBackground()
    {

    }

    @Override
    public void addButtons()
    {
        this.resetButton = aMenuController.getResetButton();
        this.exitButton = aMenuController.getExitButton();
    }

    @Override
    public void assembleMenu()
    {
        uiStage.addActor(resetButton);
        uiStage.addActor(exitButton);

    }

    @Override
    public void addListeners()
    {
        resetButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                resetBoard();
            }
        });

        exitButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                changeScreens();
            }
        });
    }

    @Override
    public void addLabels()
    {

    }

    @Override
    public void addTextFields()
    {

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
     * Anything in this method will automatically start when this object screen opens.
     * Must put listeners for buttons/etc in here otherwise there will be processing delays.
     * DO NOT PUT Listeners in render()
     */
    @Override
    public void show()
    {
        Gdx.input.setInputProcessor(uiStage); //Without this, buttons and etc will not have their event listeners activated
        game.scaryMusic.play();
        game.scaryMusic.setVolume(0.1f);
        assembleMenu();
        addListeners();
    }

    /**
     * Finds collisions among the players
     *
     * @return boolean
     */
    public boolean findCollisions()
    {
        return aBoardController.playerConflict();
    }


    @Override
    public void render(float delta)
    {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //theCamera.update();
        aShape.setProjectionMatrix(game.camera.combined);

        //Next few lines Draws Players and rectangles on board
        aShape.begin(ShapeRenderer.ShapeType.Line);
        aBoardController.drawBoard(aShape); //Draws rectangles
        aShape.setAutoShapeType(true);
        aShape.set(ShapeRenderer.ShapeType.Filled);
        aBoardController.drawConflict(aShape);
        aBoardController.drawPlayers(aShape);
        aShape.end();

        game.batch.begin();
        aBoardController.drawBoard(game.batch); //Draws tree textures on board
        aBoardController.drawDirections();
        this.arrowKeyFont.setColor(Color.MAGENTA);
        aBoardController.drawStatistics(statisticsFunc);
        if (columns > 20) {
            if (playerUp != null) {
                playerUp.drawText(game.playerFont, game.batch);
            }
        } else {
            aBoardController.drawPlayerText(game.batch);
        }
        game.batch.end();

        uiStage.act();
        uiStage.draw();
        update(delta); //Used for various keyboard input

        if (stateOfGame == State.FOUND)
        {
            game.batch.begin();
            aBoardController.drawCollision(foundFunc);
            game.batch.end();
        }

        if (stateOfGame == State.PLACEMENT)
        {
            game.batch.begin();
            aBoardController.drawPlacementInstructions(placementFunc);
            game.batch.end();
        }

    }

    //Gets the position of the mouse and converts coordinates to match camera coordinates.
    public Vector3 mousePositionInWorld(OrthographicCamera camera) {
        Vector3 v = new Vector3();
        v.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(v);
        return v;
    }

    /**
     * Updates the state of the game (collisions and movement) and collects keyboard input
     */
    public void update(float deltaTime)
    {
        Input anInput = Gdx.input;
        inputTime += deltaTime;

        //This will make sure that screens don't rapidly exit
        if (inputTime > 0.5f)
        {
            if (anInput.isKeyPressed(Input.Keys.ESCAPE))
            {
                changeScreens();
                inputTime = 0;
            }
        }
        //Will NOT unpause game if collision is found, must use reset button instead
        if (stateOfGame != State.FOUND && anInput.isKeyJustPressed(Input.Keys.SPACE))
        {
            if (stateOfGame == State.RUN)
            {
                stateOfGame = State.PAUSE;
            } else
            {
                stateOfGame = State.RUN;
            }
        }

        //TODO Write a pause text when pressing spacebar



        setPlacementOfPlayers(); //This will set the placement of players

        if (stateOfGame == State.RUN)
        {
            if (anInput.isKeyPressed(Input.Keys.RIGHT))
            {
                aBoardController.increaseSpeed();
            }
            if (anInput.isKeyPressed(Input.Keys.LEFT))
            {
                aBoardController.decreaseSpeed();
            }
            aBoardController.updatePlayers();
            this.resume();
        }

        /*Checks if there is a collision and resets data if true*/
        if (findCollisions() && stateOfGame == State.RUN)
        {
            game.found.play();
            //stateOfGame = State.STOPPED;
            for (Animations anAnimations : aBoardController.collidedStars)
            {
                collisionStars.addActor(anAnimations);
            }
            uiStage.addActor(collisionStars);
            stateOfGame = State.FOUND;
            this.pause();

            aBoardController.setStatistics();
            //aBoardController.fade(aShape);
        }

        //Restarts board if the key 'R' is pressed
        if (anInput.isKeyPressed(Input.Keys.R))
        {
            resetBoard();
        }
    }

    /**
     * Places players on the screen depending on where the screen is selected
     */
    private void setPlacementOfPlayers()
    {
        /*The following code sets the placement of player objects on a screen*/
        if (stateOfGame == State.PLACEMENT)
        {
            Vector3 v = mousePositionInWorld(game.camera); //Gathers mouse position
            int mouseX = (int) v.x;
            int mouseY = (int) v.y;
            int xArrayLocation = (int) (mouseX / aBoardController.pixelBlockWidth);
            int yArrayLocation = (int) (mouseY / aBoardController.pixelBlockHeight);

            if (columns > 20) {
                this.playerUp = aBoardController.findPlayer(xArrayLocation, yArrayLocation);
            }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                //System.out.printf("%d / %f = %d%n", mouseX, aBoardController.pixelBlockWidth, xArrayLocation); <------------GWT does not like printf()
                //System.out.printf("%d / %f = %d%n", mouseY, aBoardController.pixelBlockHeight, yArrayLocation);

                aBoardController.createUpdatePlayer(xArrayLocation, yArrayLocation, changeMovement);
                aBoardController.resetStatistics();
            }
            /*Pressing Enter will enter the game into a 'Run' state*/
            if (Gdx.input.isKeyPressed(Input.Keys.ENTER) || Gdx.input.isKeyPressed(Input.Keys.NUMPAD_ENTER)) {
                if (aBoardController.aPlayers.size() >= 2) {
                    this.stateOfGame = State.RUN;
                } else {
                    game.invalidInput.play();
                }
            }
        }
    }

    /**
     * This will change the current screen back to the previous screen.
     */
    private void changeScreens()
    {
        stateOfGame = State.STOPPED;
        this.game.setScreen(returnScreen);
    }

    /**
     * This is a Libgdx
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height)
    {
        game.aViewport.update(width, height);
        uiStage.getViewport().update(width, height);
    }

    /**
     * Resets the board and player location to defaults
     */
    private void resetBoard()
    {

        //aBoardController.createArrayOfTextures(game.boardTextures);
        aBoardController.clearCollisionAnimation(collisionStars, aBoardController.collidedStars);
        aBoardController.collidedStars.clear();
        aBoardController.resetPlayers();
        stateOfGame = beginningState;
        aBoardController.totalPlayerMovements = 0;
        aBoardController.playerUpdateTime = 0.3f;
        this.pause();

    }

    @Override
    public void pause()
    {
        game.scaryMusic.pause();
    }

    @Override
    public void resume()
    {
        game.scaryMusic.play();
    }

    @Override
    public void hide()
    {
        game.scaryMusic.stop();
    }

    /**
     * Must dispose of game objects when finished, otherwise they can linger in the background
     */
    @Override
    public void dispose()
    {
        aShape.dispose();
        returnScreen.dispose();
        uiStage.dispose();
        game.dispose();

    }
}
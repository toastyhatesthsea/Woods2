package com.woods.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/**
 * This class represents an information screen that gives some details about the game
 * and what each gamemode for each grade entails.
 */
public class InfoScreen implements Screen, Menu
{

    /**
     * State of the screen
     */
    public enum State
    {
        PAUSE,
        RUN,
        RESUME,
        STOPPED,
        FOUND,
        SELECTION,
        K2,
        INFO,
        THREE_TO_FIVE,
        SIX_TO_EIGHT
    }

    Stage backGroundStage, uiStage; //Stages are used to add elements to the screen
    Woods aGame;
    Screen returnScreen;
    float animationStatetime;
    Label gameInfoLabel, kindergartenLabel, threeToFiveLabel, sixToEightLabel; //Labels are just text to be drawn to the screen

    Button backButton, infoButton, kindergartenInfoButton, okayButton, threeToFiveInfoButton, sixToEightInfoButton; //Buttons that are drawn to the screen
    Background raindropsBackground;
    MenuController aMenuController;
    Board screenDimensions;
    Group treeGroupBackground; //Just used for background tree images
    Group uiButtons, gradeButtons, gradeInfoGroup, standardInfoGroup;
    State gameState; //The current state of the game


    final float WORLD_WIDTH = 50;
    final float WORLD_HEIGHT = 50;

    public InfoScreen(Woods aGame, Screen returnScreen)
    {
        this.gameState = State.INFO; //Default state
        this.returnScreen = returnScreen;
        this.aGame = aGame;
        this.animationStatetime = 0;
        this.screenDimensions = new Board((int) WORLD_HEIGHT, (int) WORLD_WIDTH,
                aGame.camera.viewportWidth / WORLD_WIDTH, aGame.camera.viewportHeight / WORLD_HEIGHT);
        backGroundStage = new Stage(aGame.aViewport);
        uiStage = new Stage(aGame.aViewport);
        aMenuController = new MenuController(aGame, returnScreen);
        uiButtons = new Group();
        gradeButtons = new Group();
        gradeInfoGroup = new Group();
        addButtons();
        addLabels();
        addButtons();
        addBackground();
        assembleMenu();

    }

    /**
     * Removes game info label according to the game state
     */
    private void removeLabelGroup()
    {
        if (gameState == State.K2)
        {
            gradeInfoGroup.removeActor(kindergartenLabel);
        } else if (gameState == State.THREE_TO_FIVE)
        {
            gradeInfoGroup.removeActor(threeToFiveLabel);
        } else if (gameState == State.SIX_TO_EIGHT)
        {
            gradeInfoGroup.removeActor(sixToEightLabel);
        }
    }

    @Override
    public void addBackground()
    {
        raindropsBackground = aMenuController.getRainDropsBackground();
        treeGroupBackground = aMenuController.getBackgroundTreeImageGroup();
    }

    @Override
    public void addButtons()
    {
        okayButton = aMenuController.getOkayButton();
        backButton = aMenuController.getBackButton();
        kindergartenInfoButton = aMenuController.getImageOfBunny();
        threeToFiveInfoButton = aMenuController.getImageOfPig();
        sixToEightInfoButton = aMenuController.getImageOfCow();
        /*Adds the grade image buttons to a grade buttons group */
        gradeButtons.addActor(sixToEightInfoButton);
        gradeButtons.addActor(kindergartenInfoButton);
        gradeButtons.addActor(threeToFiveInfoButton);

        infoButton = aMenuController.getInfoButton();
        infoButton.setSize(infoButton.getWidth() * 1.5f,
                infoButton.getHeight() * 1.5f);
        infoButton.setX(screenDimensions.blockPixelWidth * 25 - infoButton.getWidth());
        infoButton.setY(screenDimensions.blockPixelHeight * 49 - infoButton.getHeight());

        okayButton.setSize(screenDimensions.blockPixelWidth * 10,
                screenDimensions.blockPixelHeight * 8);
        okayButton.setX(screenDimensions.blockPixelWidth * 48 - okayButton.getWidth());
        okayButton.setY(screenDimensions.blockPixelHeight * 28);


    }

    @Override
    public void assembleMenu()
    {
        gradeInfoGroup.addActor(gameInfoLabel);
        uiButtons.addActor(gradeButtons);
        uiStage.addActor(backButton);
        uiStage.addActor(gradeInfoGroup);
        uiStage.addActor(uiButtons);
        backGroundStage.addActor(treeGroupBackground);
        backGroundStage.addActor(infoButton);
    }

    @Override
    public void addListeners()
    {
        backButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                aGame.setScreen(returnScreen);
            }
        });

        kindergartenInfoButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                removeLabelGroup();
                gameState = State.K2;
                gradeInfoGroup.addActor(kindergartenLabel);
                gradeInfoGroup.removeActor(gameInfoLabel);
                gradeButtons.addActor(okayButton);
            }
        });

        threeToFiveInfoButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                removeLabelGroup();
                gameState = State.THREE_TO_FIVE;
                gradeInfoGroup.removeActor(gameInfoLabel);
                gradeButtons.addActor(okayButton);
                gradeInfoGroup.addActor(threeToFiveLabel);
            }
        });

        sixToEightInfoButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                removeLabelGroup();
                gameState = State.SIX_TO_EIGHT;
                gradeInfoGroup.removeActor(gameInfoLabel);
                gradeButtons.addActor(okayButton);
                gradeInfoGroup.addActor(sixToEightLabel);
            }
        });

        okayButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                gradeInfoGroup.addActor(gameInfoLabel);
                removeLabelGroup();
                gameState = State.INFO;
                gradeButtons.removeActor(okayButton);
            }
        });
    }

    @Override
    public void addLabels()
    {
        gameInfoLabel = aMenuController.getGameInfoLabel();
        gameInfoLabel.setPosition(screenDimensions.blockPixelWidth * 5,
                screenDimensions.blockPixelHeight * 25);
        kindergartenLabel = aMenuController.getK2InfoLabel();
        kindergartenLabel.setPosition(screenDimensions.blockPixelWidth * 5,
                screenDimensions.blockPixelHeight * 25);
        threeToFiveLabel = aMenuController.getThreeToFiveInfoLabel();
        threeToFiveLabel.setPosition(screenDimensions.blockPixelWidth * 5,
                screenDimensions.blockPixelHeight * 25);
        sixToEightLabel = aMenuController.getSixToEightInfoLabel();
        sixToEightLabel.setPosition(screenDimensions.blockPixelWidth * 5,
                screenDimensions.blockPixelHeight * 25);
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

    @Override
    public void show()
    {
        Gdx.input.setInputProcessor(uiStage); //Sets which stage the 'Listeners' will activate on
        aGame.forestMusic.play();
        aGame.forestMusic.setVolume(0.1f);
        addListeners();
    }

    /**
     * Draws to the screen, a rasterizer
     *
     * @param delta float
     */
    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        aGame.camera.update();
        aGame.batch.setProjectionMatrix(aGame.camera.combined);
        raindropsBackground.draw(aGame.batch, animationStatetime += delta);
        backGroundStage.act();
        backGroundStage.draw();

        uiStage.act();
        uiStage.draw();

        update();
    }

    private void update()
    {
        if (gameState == State.INFO)
        {

        }
    }

    @Override
    public void resize(int width, int height)
    {
        uiStage.getViewport().update(width, height);
        backGroundStage.getViewport().update(width, height);
        screenDimensions = new Board(50, 50,
                (float) width / 50, (float) height / 50);
        this.aGame.aViewport.update(width, height);
    }

    @Override
    public void pause()
    {
        aGame.forestMusic.stop();
    }

    @Override
    public void resume()
    {
        aGame.forestMusic.play();
    }

    @Override
    public void hide()
    {

    }

    @Override
    public void dispose()
    {
        aGame.forestMusic.stop();
    }

}

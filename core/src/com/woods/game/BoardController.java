package com.woods.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

import java.util.*;

/**
 * This is a Board controller class that controls the state of the game board.
 */
public class BoardController
{

    Woods game;
    BoardOfPieces tileBoard;
    Board playerBoard;
    ArrayList<Player> collidedLocations; //Used to remember location of player conflicts
    int numberOfRows;
    int numberOfColumns;
    float pixelBlockWidth;
    float pixelBlockHeight;
    Array<Animations> collidedStars; //Used to draw animated stars on board when there is a collision
    List<Player> aPlayers = new ArrayList<>();
    List<Player> beginningPlayers = new ArrayList<>();
    int totalPlayerMovements;
    float totalTimesRan, totalMovements, average, highest, lowest;
    MenuController aMenuController;

    Music adventureMusic;

    float playerUpdateTime;
    float playerMovementTimer;


    public BoardController(Woods aGame, int numberOfRows, int numberOfColumns, float pixelBlockWidth, float pixelBlockHeight)
    {
        this.game = aGame;
        this.tileBoard = new BoardOfPieces(numberOfRows, numberOfColumns, pixelBlockWidth, pixelBlockHeight);
        this.playerBoard = new Board(numberOfRows, numberOfColumns, pixelBlockWidth, pixelBlockHeight);
        this.collidedLocations = new ArrayList<>();
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.pixelBlockWidth = pixelBlockWidth;
        this.pixelBlockHeight = pixelBlockHeight;
        this.totalPlayerMovements = 0;
        this.highest = 0;
        this.lowest = 0;
        this.playerUpdateTime = .3f; //Will update player movement every .3 seconds between rendering frames
        this.adventureMusic = Gdx.audio.newMusic(Gdx.files.internal("brazilian.mp3"));
        this.aMenuController = new MenuController(aGame, new MenuScreen(game));
        collidedStars = new Array<>();
        resetStatistics();
    }

    /**
     * Resets statistics
     */
    public void resetStatistics() {
        this.totalPlayerMovements = 0;
        this.totalTimesRan = 0;
        this.totalMovements = 0;
        this.average = 0;
        this.highest = 0;
        this.lowest = 0;
    }

    /**
     * Clones players to memorize their original location before moving. Very useful when not using default player starting locations,
     * such as the corners of the board
     */
    private void clonePlayers() {
        beginningPlayers = new ArrayList<>();
        for (Player player : aPlayers) {
            this.beginningPlayers.add(new Player(player));
        }
    }

    public void createUpdatePlayer(int xArrayLocation, int yArrayLocation, boolean changeMovement){
        resetStatistics(); //Reset average if player configuration changes.
        this.collidedLocations = new ArrayList<>();
        Player player = findPlayer(xArrayLocation, yArrayLocation);
        if (player == null){
            aPlayers.add(new Player(xArrayLocation, yArrayLocation, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, true));
        } else {
            if (player.hasNextMovement() && changeMovement){
                player.nextMovement();
            } else {
                aPlayers.remove(player);
            }
        }
        clonePlayers();
    }

    public boolean isSwapCollision(Player self){
        int xCurrentP1 = self.getxArrayLocation();
        int yCurrentP1 = self.getyArrayLocation();

        for (Player player : aPlayers) {
            int xPastP2 = player.getPreviousX();
            int yPastP2 = player.getPreviousY();

            //If player is in another player's previous location
            if (self != player && xPastP2 == xCurrentP1 && yPastP2 == yCurrentP1){
                int xPastP1 = self.getPreviousX();
                int yPastP1 = self.getPreviousY();

                int xCurrentP2 = player.getxArrayLocation();
                int yCurrentP2 = player.getyArrayLocation();

                //And if the other player is in the player's previous location
                if(xPastP1 == xCurrentP2 && yPastP1 == yCurrentP2) {
                    //Determines and moves colliding players to the middle of their locations.
                    player.setxArrayLocation(self.xArrayLocation);
                    player.setyArrayLocation(self.yArrayLocation);
                    return true; //They swapped locations and should have collided.
                }
            }
        }
        return false; //No swapping collisions found
    }

    public Player findPlayer(int xArrayLocation, int yArrayLocation) {
        for (Player player : aPlayers) {
            if (player.getxArrayLocation() == xArrayLocation && player.getyArrayLocation() == yArrayLocation){
                return player;
            }
        }
        return null;
    }

    /**
     * Creates players at RANDOM locations on the board using Player objects and stores them in an array
     */
    public void createPlayersRandomLocations(int numberOfPlayers)
    {
        aPlayers.clear();
        Random aRan = new Random();
        int currentPlayersAdded = 0;

        //This loop will add random players and makes sure that it doesn't add on the same location
        while (currentPlayersAdded < numberOfPlayers)
        {
            int xArrayLocation = aRan.nextInt(numberOfColumns);
            int yArrayLocation = aRan.nextInt(numberOfRows);

            if (playerBoard.boardArray[yArrayLocation][xArrayLocation] == null)
            {
                Player aPlayer = new Player(xArrayLocation, yArrayLocation, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, false);
                aPlayers.add(aPlayer);
                playerBoard.boardArray[yArrayLocation][xArrayLocation] = aPlayer;
                currentPlayersAdded++;
            }
        }
        clonePlayers();
    }

    /**
     * Creates players at default locations. Which are the corners of the board.
     */
    public void createPlayersDefaultLocation(int numberOfPlayers) throws IllegalArgumentException
    {
        if (numberOfPlayers > 4)
            throw new IllegalArgumentException("Too many players and not enough corners, for default corner locations");

        aPlayers.clear();

        switch (numberOfPlayers){
            case 4:
                aPlayers.add(new Player(0, 0, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, false));
            case 3:
                aPlayers.add(new Player(numberOfColumns - 1, numberOfRows - 1, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, false));
            case 2:
                aPlayers.add(new Player(numberOfColumns - 1, 0, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, false));
            case 1:
                aPlayers.add(new Player(0, numberOfRows - 1, Color.FIREBRICK, pixelBlockWidth, pixelBlockHeight, false));
        }
        clonePlayers();
    }

    /**
     * Checks for player collisions using just array locations. Should check for collisions
     * after moving players
     *
     * @return boolean
     */
    public boolean playerConflict() {;
        boolean collision = false;
        ArrayList<Player> playerConflictArrayList = new ArrayList<>();
        for (Player aPlayer : aPlayers) {
            collision = playerConflictArrayList.contains(aPlayer) ||  /*Players are on same spot*/
                    isSwapCollision(aPlayer); /*Players swap locations (meaning they pass each other).*/
            if (collision) {
                collidedLocations.add(new Player(aPlayer.xArrayLocation, aPlayer.yArrayLocation, new Color(Color.LIGHT_GRAY),
                        aPlayer.width, aPlayer.height, false));
                Animations aStar = aMenuController.createStarAnimations();
                //Sets the location and size of where an animation should occur when a collision happens
                aStar.setLocationAndSize(aPlayer.xArrayLocation, aPlayer.yArrayLocation, aPlayer.width, aPlayer.height);
                collidedStars.add(aStar);
                break;
            } else {
                playerConflictArrayList.add(aPlayer);
            }
        }
        return collision;
    }

    /**
     * Draws all the previous and current conflict on the board
     */
    public void drawConflict(ShapeRenderer aRenderer)
    {
        for (Player aPlayer : collidedLocations)
        {
            aPlayer.draw(aRenderer);
        }
    }

    /**
     * Draws collision text
     * @param foundFunction found
     */
    public void drawCollision(found foundFunction)
    {
        foundFunction.drawCollision(game.batch);
    }

    /**
     * Draws instructional placement information
     * @param placementFunction
     */
    public void drawPlacementInstructions(placement placementFunction)
    {
        placementFunction.drawPlacementInstructions(game.batch);
    }

    public void drawStatistics(statistics aStatFunction)
    {
        aStatFunction.drawStatistics(game.batch);
    }

    /**
     * Draws directions for players to control game
     */
    public void drawDirections()
    {
        game.medievalFont.setColor(1, 1, 0, 1f);
        game.medievalFont.draw(game.batch, "Press ", game.camera.viewportWidth / 2 - 500, 50);
        game.medievalFont.setColor(Color.WHITE);
        game.medievalFont.draw(game.batch, "Left Arrow Key to slow", game.camera.viewportWidth / 2 - 400, 50);
        game.medievalFont.setColor(1, 1, 0, 1f);

        game.medievalFont.draw(game.batch, " or ", game.camera.viewportWidth / 2 - 50, 50);
        game.medievalFont.setColor(Color.WHITE);
        game.medievalFont.draw(game.batch, " Right Arrow Key ", game.camera.viewportWidth / 2, 50);
        game.medievalFont.setColor(1, 1, 0, 1f);
        game.medievalFont.draw(game.batch, " to increase speed", game.camera.viewportWidth / 2 + 275, 50);


        game.medievalFont.draw(game.batch, "Press R to Reset", game.camera.viewportWidth - 275, 75);

        game.medievalFont.draw(game.batch, "ESC to exit", 0, 75);
    }

    /**
     * Creates an array of GraphicsTiles objects. Using the i and j values in the loops for x/y tile location
     */
    public void createArray()
    {
        for (int i = 0; i < tileBoard.getPiecesArray().length; i++)
        {
            //TODO Fix color, always adding alpha of 0.1f even on reset
            for (int j = 0; j < tileBoard.getPiecesArray()[i].length; j++)
            {
                GraphicsTile aTile = new GraphicsTile(j, i, Color.GRAY, pixelBlockWidth, pixelBlockHeight);
                Pieces somePiece = new Pieces();
                tileBoard.getPiecesArray()[i][j] = somePiece;
                somePiece.addPiece(aTile);
            }
        }
    }

    /**
     * Removes a collision animation from the board
     * @param starGroup Group
     * @param arrayOfActors Array<Animations>
     */
    public void clearCollisionAnimation(Group starGroup, Array<Animations> arrayOfActors)
    {
        for (Actor anActor : arrayOfActors)
        {
            starGroup.removeActor(anActor);
        }
    }

    /**
     * Takes an array of textures and randomly places them on every block on the gameboard
     *
     * @param textureArray Texture[]
     */
    public void createArrayOfTextures(Texture[] textureArray)
    {
        Random aRan = new Random();
        Texture aTexture;
        int arrayTextureIndex;
        GraphicsTile aRectTile;
        TextureTile aTile;
        Pieces somePiece;

        for (int i = 0; i < tileBoard.getPiecesArray().length; i++)
        {
            for (int j = 0; j < tileBoard.getPiecesArray()[i].length; j++)
            {
                arrayTextureIndex = aRan.nextInt(10);
                aTexture = textureArray[arrayTextureIndex]; //Too many different textures on large super large boards will slow down HTML build, GWT is slow. Would have to convert to maptiles instead

                aTile = new TextureTile(j, i, pixelBlockWidth, pixelBlockHeight, Color.GRAY, aTexture);
                aRectTile = new GraphicsTile(j, i, Color.GRAY, pixelBlockWidth, pixelBlockHeight);
                somePiece = new Pieces();
                tileBoard.getPiecesArray()[i][j] = somePiece;
                somePiece.addPiece(aTile);
                somePiece.addPiece(aRectTile);

            }
        }
    }

    /**
     * Draws basic rectangle shapes on the screen
     * @param renderer ShapeRenderer
     */
    public void drawBoard(ShapeRenderer renderer)
    {
        for (int i = 0; i < tileBoard.getPiecesArray().length; i++)
        {
            for (int j = 0; j < tileBoard.getPiecesArray()[i].length; j++)
            {

                Pieces somePiece = tileBoard.getPiecesArray()[i][j];
                somePiece.draw(renderer);
                //aBlock.draw(renderer);
            }
        }
    }

    /**
     * Draws each individual Piece on the board using the Pieces data structure.
     * Must be used between a SpriteBatch.Begin() and SpriteBatch.End()
     *
     * @param aBatch SpriteBatch
     */
    public void drawBoard(SpriteBatch aBatch)
    {
        for (int i = 0; i < tileBoard.getPiecesArray().length; i++)
        {
            for (int j = 0; j < tileBoard.getPiecesArray()[i].length; j++)
            {

                Pieces somePiece = tileBoard.getPiecesArray()[i][j];
                somePiece.draw(aBatch);
                //aBlock.draw(renderer);
            }
        }
    }

    /**
     * Draws each player that is located in the Players ArrayList
     * MUST be used between Sprit
     *
     * @param renderer
     */
    public void drawPlayers(ShapeRenderer renderer)
    {
        for (Player somePlayer : aPlayers)
        {
            somePlayer.draw(renderer);
        }
    }

    /**
     * Draws text for the type of 'player'
     * @param batch SpriteBatch
     */
    public void drawPlayerText(SpriteBatch batch) {
        for (Player somePlayer : aPlayers)
        {
            somePlayer.drawText(game.playerFont, batch);
        }
    }

    public void fade(ShapeRenderer renderer)
    {
        for (int i = 0; i < tileBoard.getPiecesArray().length; i++)
        {
            for (int j = 0; j < tileBoard.getPiecesArray()[i].length; j++)
            {
                Pieces somePiece = tileBoard.getPiecesArray()[i][j];
                somePiece.fade(renderer);
                //aBlock.draw(renderer);
            }
        }
    }

    /**
     * Decrease speed of player movement
     */
    public void decreaseSpeed()
    {
        playerUpdateTime += 0.01f;
    }

    /**
     * Increase speed of player movement
     */
    public void increaseSpeed()
    {
        if ((playerUpdateTime - 0.01f) >= 0.0f)
        {
            playerUpdateTime -= 0.01f;
        }
    }

    public Music getAdventureMusic()
    {
        return adventureMusic;
    }

    public float getAverage()
    {
        return average;
    }

    public void setStatistics()
    {
        this.totalTimesRan++;
        this.totalMovements += totalPlayerMovements; //Adds all the player movements so far in the game
        this.average = this.totalMovements / this.totalTimesRan;
        if (totalPlayerMovements > highest)
        {
            this.highest = totalPlayerMovements;
        }
        if (totalPlayerMovements < lowest)
        {
            this.lowest = this.totalPlayerMovements;
        }
        if (lowest == 0) //Sets lowest if the game just started
        {
            this.lowest = this.totalPlayerMovements;
        }
    }

    /**
     * Updates location of players per game delta time
     */
    public void updatePlayers()
    {
        playerMovementTimer += Gdx.graphics.getDeltaTime(); //Gets the time from last render

        if (playerMovementTimer >= playerUpdateTime) //Updates movement every .3 seconds
        {
            for (Player somePlayer : aPlayers)
            {
                somePlayer.playerMovement(numberOfRows, numberOfColumns);
            }
            totalPlayerMovements++;
            playerMovementTimer = 0;
        }
    }

    public void resetPlayers() {
        aPlayers = this.beginningPlayers;
        clonePlayers();
    }
}

interface found
{
    void drawCollision(SpriteBatch aBatch);
}

interface placement
{
    void drawPlacementInstructions(SpriteBatch aBatch);
}

interface statistics
{
    void drawStatistics(SpriteBatch aBatch);
}
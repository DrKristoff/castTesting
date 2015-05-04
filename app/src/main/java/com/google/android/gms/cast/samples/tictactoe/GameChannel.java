/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.cast.samples.tictactoe;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * An abstract class which encapsulates control and game logic for sending and receiving messages
 * during a TicTacToe game.
 */
public abstract class GameChannel implements Cast.MessageReceivedCallback {
	
    private static final String TAG = GameChannel.class.getSimpleName();

    private static final String GAME_NAMESPACE = "urn:x-cast:com.betonit";

    // Receivable event types
    private static final String KEY_EVENT = "event";
    private static final String KEY_JOINED = "joined";
    private static final String KEY_ENDGAME = "endgame";
    private static final String KEY_BET_REQUEST = "bet_request";
    private static final String KEY_GUESS_REQUEST = "guess_request";
    private static final String KEY_ERROR = "error";

    // Commands
    private static final String KEY_COMMAND = "command";
    private static final String KEY_JOIN = "join";
    private static final String KEY_BET = "bet";
    private static final String KEY_GUESS = "guess";
    private static final String KEY_LEAVE = "leave";
    

    private static final String KEY_GAME_OVER = "game_over";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_NAME = "name";
    private static final String KEY_PLAYER = "player";
    private static final String KEY_ANSWER_ONE = "answer_one";
    private static final String KEY_ANSWER_TWO = "answer_two";

    /**
     * Constructs a new GameChannel m with GAME_NAMESPACE as the namespace used by
     * the superclass.
     */
    protected GameChannel() {
    }

    /**
     * Performs some action upon a player joining the game.
     *
     * @param playerSymbol either X or O
     * @param opponentName the name of the player who just joined an existing game, or the opponent
     */
    protected abstract void onGameJoined(String playerSymbol, String opponentName);


    /**
     * Performs some action upon game end, depending on game's end state and the position of the
     * winning pieces.
     *
     * @param endState likely to be END_STATE_X_WON, END_STATE_O_WON, or END_STATE_ABANDONED
     * @param location an int value corresponding to the enum WinningLocation's values
     */
    protected abstract void onGameEnd(String endState, int location);

    /**
     * 
     *
     * 
     */
    protected abstract void onBetRequest();
    
    /**
     * 
     *
     * 
     */
    protected abstract void onGuessRequest();            

    /**
     * Performs some action upon a game error.
     *
     * @param errorMessage the string description of the error
     */
    protected abstract void onGameError(String errorMessage);

    /**
     * Returns the namespace for this cast channel.
     */
    public String getNamespace() {
        return GAME_NAMESPACE;
    }

    /**
     * Attempts to connect to an existing session of the game by sending a join command.
     *
     * @param name the name of the player that is joining
     */
    public final void join(GoogleApiClient apiClient, String name) {
        try {
            Log.d(TAG, "join: " + name);
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, KEY_JOIN);
            payload.put(KEY_NAME, name);
            sendMessage(apiClient, payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to join a game", e);
        }
    }

    /**
     * Attempts to place a bet by submitting the answer choices and number of coins bet.
     *
     * @param answerOne the value of the first answer you are betting on
     * @param answerOne the number of coins you are betting on the first answer
     * @param answerOne the value of the second answer you are betting on
     * @param answerOne the number of coins you are betting on the second answer
     *
     */
    public final void bet(GoogleApiClient apiClient, final int answerOne, final int answerOneCoins,
                          final int answerTwo, final int answerTwoCoins) {
        Log.d(TAG, "bet: " + answerOne + " with " + answerOneCoins + " coins");
        Log.d(TAG, "bet: " + answerTwo + " with " + answerTwoCoins + " coins");

        try {
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, KEY_BET);

            JSONArray arrayOne = new JSONArray();
            arrayOne.put(answerOne);
            arrayOne.put(answerOneCoins);

            JSONArray arrayTwo = new JSONArray();
            arrayTwo.put(answerTwo);
            arrayTwo.put(answerTwoCoins);

            payload.put(KEY_ANSWER_ONE, arrayOne);
            payload.put(KEY_ANSWER_TWO, arrayTwo);
            sendMessage(apiClient, payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to place a bet", e);
        }
    }
    
    /**
     * Attempts to guess a numeric answer to the question asked by the game.
     *
     * @param guess the value of the submitted guess
     *
     */
    public final void guess(GoogleApiClient apiClient, final int guess) {
        Log.d(TAG, "guess: " + guess);

        try {
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, KEY_GUESS);
            payload.put(KEY_GUESS, guess);
            sendMessage(apiClient, payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to make a guess", e);
        }
    }

    /**
     * Sends a command to leave the current game.
     */
    public final void leave(GoogleApiClient apiClient) {
        try {
            Log.d(TAG, "leave");
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, KEY_LEAVE);
            sendMessage(apiClient, payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to leave a game", e);
        }
    }
W
    /**
     * Processes all Text messages received from the receiver device and performs the appropriate
     * action for the message. Recognizable messages are of the form:
     *
     * <ul>
     * <li> KEY_JOINED: a player joined the current game
     * <li> KEY_ENDGAME: the game has ended in one of the END_STATE_* states
     * <li> KEY_ERROR: a game error has occurred
     * <li> KEY_GUESS_REQUEST: the game has finished asking a question and requests answers
     * <li> KEY_BET_REQUEST: everyone has submitted guesses and the game requests bets to be placed
     * </ul>
     *
     * <p>No other messages are recognized.
     */
    @Override
    public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        try {
            Log.d(TAG, "onTextMessageReceived: " + message);
            JSONObject payload = new JSONObject(message);
            Log.d(TAG, "payload: " + payload);
            if (payload.has(KEY_EVENT)) {
                String event = payload.getString(KEY_EVENT);
                
                /**********************JOINED EVENT***************************/
                if (KEY_JOINED.equals(event)) {
                    Log.d(TAG, "JOINED");
                    try {
                        String player = payload.getString(KEY_PLAYER);
                        String opponentName = payload.getString(KEY_OPPONENT);
                        onGameJoined(player, opponentName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } 
                /**********************ENDGAME EVENT***************************/
                else if (KEY_ENDGAME.equals(event)) {
                    Log.d(TAG, "ENDGAME");
                    try {
                        String endState = payload.getString(KEY_END_STATE);
                        int winningLocation = -1;
                        if (END_STATE_ABANDONED.equals(endState) == false) {
                            winningLocation = payload.getInt(KEY_WINNING_LOCATION);
                        }
                        onGameEnd(endState, winningLocation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } 
                /**********************ERROR EVENT***************************/
                else if (KEY_ERROR.equals(event)) {
                    Log.d(TAG, "ERROR");
                    try {
                        String errorMessage = payload.getString(KEY_MESSAGE);
                        onGameError(errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } 
                
                /********************GUESS REQUEST EVENT*******************/
                else if (KEY_GUESS_REQUEST.equals(event)) {
                    Log.d(TAG, "ERROR");
                    try {
                        String errorMessage = payload.getString(KEY_MESSAGE);
                        onGameError(errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } 
                
                /**********************BET REQUEST EVENT************************/
                else if (KEY_BET_REQUEST.equals(event)) {
                    Log.d(TAG, "ERROR");
                    try {
                        String errorMessage = payload.getString(KEY_MESSAGE);
                        onGameError(errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } 
            } 
            /**********************UNKNOWN EVENT***************************/
            else {
                Log.w(TAG, "Unknown payload: " + payload);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Message doesn't contain an expected key.", e);
        }
    }

    private final void sendMessage(GoogleApiClient apiClient, String message) {
        Log.d(TAG, "Sending message: (ns=" + GAME_NAMESPACE + ") " + message);
        Cast.CastApi.sendMessage(apiClient, GAME_NAMESPACE, message).setResultCallback(
                new SendMessageResultCallback(message));
    }

    private final class SendMessageResultCallback implements ResultCallback<Status> {
        String mMessage;

        SendMessageResultCallback(String message) {
            mMessage = message;
        }

        @Override
        public void onResult(Status result) {
            if (!result.isSuccess()) {
                Log.d(TAG, "Failed to send message. statusCode: " + result.getStatusCode()
                        + " message: " + mMessage);
            }
        }
    }

}

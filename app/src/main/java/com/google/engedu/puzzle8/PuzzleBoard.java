/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    private int steps = 0;
    private PuzzleBoard previousBoard = null;

    // Take the passed-in Bitmap object and divide it into NUM_TILES x NUM_TILES equal-sized pieces
    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        int totalTiles = NUM_TILES * NUM_TILES;
        int chunkSize = parentWidth / 3;
        tiles = new ArrayList<>(totalTiles);

        for (int i = 0; i < totalTiles; i++) {
            Bitmap chunk = Bitmap.createBitmap(
                    bitmap,
                    (i % NUM_TILES) * chunkSize,
                    (i / NUM_TILES) * chunkSize,
                    chunkSize,
                    chunkSize);

            // Leave the last tile null to represent the 'empty' tile!
            if (totalTiles == i + 1) {
                tiles.add(null);
            } else {
                tiles.add(new PuzzleTile(chunk, i));
            }
        }
    }

    PuzzleBoard(PuzzleBoard otherBoard, int steps) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        previousBoard = otherBoard;
        this.steps = steps + 1;
    }

    public void reset() {
        // Reset steps and previous board.
        steps = 0;
        previousBoard = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> possibleMoves = new ArrayList<>();

        // Locate an empty square in the current board
        int emptySquare = 9;
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            if (tiles.get(i) == null) {
                emptySquare = i;
                break;
            }
        }

        // Consider all the neighbours of the empty square (using the NEIGHBOUR_COORDS array)
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = emptySquare % NUM_TILES + delta[0];
            int nullY = emptySquare / NUM_TILES + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES) {
                // If the neighbouring square is valid (within the boundaries of the puzzle),
                // make a copy of the current board (using the provided copy constructor),
                // move the tile in that square to the empty square and add
                // this copy of the board to the list of neighbours to be returned.
                PuzzleBoard puzzleBoard = new PuzzleBoard(PuzzleBoard.this, steps);
                puzzleBoard.swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(emptySquare % NUM_TILES, emptySquare / NUM_TILES));
                possibleMoves.add(puzzleBoard);
            }
        }
        return possibleMoves;
    }

    // Returns the sum of the distances determined by Manhattan priority function
    // (sum of the vertical and horizontal distance) from the blocks to their goal positions,
    // plus the number of moves made so far to get to the state.
    public int priority() {
        int priority = 0;

        // Manhattan priority function
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                int actualX = i % NUM_TILES;
                int actualY = i / NUM_TILES;
                int positionX = tile.getNumber() % NUM_TILES;
                int positionY = tile.getNumber() / NUM_TILES;

                priority = priority + Math.abs(positionX - actualX) + Math.abs(positionY - actualY);
            }
        }

        // Return the Manhattan distance + steps.
        return priority + steps;
    }

    public PuzzleBoard getPreviousBoard() {
        return previousBoard;
    }
}

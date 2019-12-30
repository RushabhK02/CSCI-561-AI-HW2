import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class MinMaxMetadata {
	double alpha = Integer.MIN_VALUE;
	double beta = Integer.MAX_VALUE;
	double evalValue = Integer.MIN_VALUE;
	String[] soln = new String[0];
}

class BoardPiece {
	int row;
	int col;
	double evalValue;
	double moveDistance;
	char move;
	List<String> moves;
	BoardPiece parent = null;

	public BoardPiece(int row, int col, double evalValue, List<String> moves, char move) {
		this.row = row;
		this.col = col;
		this.evalValue = evalValue;
		this.moves = moves;
		this.move = move;
	}

	@Override
	public boolean equals(Object p) {
		boolean retVal = false;

		if (p instanceof BoardPiece) {
			BoardPiece ptr = (BoardPiece) p;
			retVal = ptr.col == this.col && ptr.row == this.row;
		}
		return retVal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * this.row + 7 * this.col;
		return hash;
	}
}

class PlayerMetadata {
	HashMap<String, BoardPiece> campPieces;
	HashMap<String, BoardPiece> pieces;
	int goalPieces = 0;
}

public class homework {
	public static String[] blackCampLoc = new String[] { "0,0", "1,0", "2,0", "3,0", "4,0", "0,1", "1,1", "2,1", "3,1",
			"4,1", "0,2", "1,2", "2,2", "3,2", "0,3", "1,3", "2,3", "0,4", "1,4" };

	public static String[] whiteCampLoc = new String[] { "15,15", "14,15", "13,15", "12,15", "11,15", "15,14", "14,14",
			"13,14", "12,14", "11,14", "15,13", "14,13", "13,13", "12,13", "15,12", "14,12", "13,12", "15,11",
			"14,11" };

	public static char[][] gameBoard = new char[16][16];
	public static double[][] playerEvalFunc = new double[16][16];
	public static double[][] opponentEvalFunc = new double[16][16];
	public static boolean playerType;
	public static String gameType;
	public static float remainingTime;
	public static int depth;
	public static boolean playDataExists;
	public static List<String> movesSoFar = new ArrayList<String>();
	public static float moveTimeLimit;
	public static long startTime;
//	public static int maxDepth = 3;

	public static void main(String[] args) {
		try {
			startTime = System.currentTimeMillis();
			String fileName = "./input.txt";
			File file = new File(fileName);
			FileReader reader;

			reader = new FileReader(file);

			BufferedReader br = new BufferedReader(reader);
			String line;
			Queue<String> lines = new LinkedList<>();
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			br.close();
			reader.close();
			
			File playData = new File("./playdata.txt");
			playDataExists = playData.exists();

			if (playDataExists) {
				reader = new FileReader(playData);
				br = new BufferedReader(reader);

				String metadata;
				Queue<String> mdEval = new LinkedList<>();
				while ((metadata = br.readLine()) != null) {
					mdEval.add(metadata);
				}

				int idx = 0;
				for (int i = 0; i < 16; i++) {
					idx = 0;
					String[] values = mdEval.remove().split("\\t+");
					for (String val : values) {
						playerEvalFunc[i][idx] = Float.parseFloat(val);
						idx++;
					}
				}

				while (!mdEval.isEmpty()) {
					movesSoFar.add(mdEval.poll());
				}

				br.close();
				reader.close();
			}

			String[] soln = solveUsingMinMax(lines);
			if (soln == null)
				soln = new String[0];

			FileWriter writer = new FileWriter(new File("./output.txt"));
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			
			for (int i=0; i<soln.length-1; i++) {
				bufferedWriter.write(soln[i]);
				bufferedWriter.newLine();
				
				String[] coords = soln[i].split(" ");
				int from_col = Integer.parseInt(coords[1].split(",")[0]);
				int from_row = Integer.parseInt(coords[1].split(",")[1]);

				int to_col = Integer.parseInt(coords[2].split(",")[0]);
				int to_row = Integer.parseInt(coords[2].split(",")[1]);

				char temp = gameBoard[from_row][from_col];
				gameBoard[from_row][from_col] = gameBoard[to_row][to_col];
				gameBoard[to_row][to_col] = temp;
//				gameBoard[from_row][from_col] = '#';
			}
            if(soln.length>0){
                bufferedWriter.write(soln[soln.length-1]);
                bufferedWriter.newLine();
                String[] coords = soln[soln.length-1].split(" ");
				int from_col = Integer.parseInt(coords[1].split(",")[0]);
				int from_row = Integer.parseInt(coords[1].split(",")[1]);

				int to_col = Integer.parseInt(coords[2].split(",")[0]);
				int to_row = Integer.parseInt(coords[2].split(",")[1]);

				char temp = gameBoard[from_row][from_col];
				gameBoard[from_row][from_col] = gameBoard[to_row][to_col];
				gameBoard[to_row][to_col] = temp;    
            }

			for (char[] row : gameBoard) {
				for (char e : row) {
					bufferedWriter.write(String.valueOf(e).toUpperCase());
				}
				bufferedWriter.newLine();
			}

			FileWriter fw = new FileWriter(playData, playDataExists);
			BufferedWriter bw = new BufferedWriter(fw);
			if (playDataExists && movesSoFar.size() < 40) {
				int[][] coords = getCoordsFromSoln(soln);
				if (!movesSoFar
						.contains("" + coords[0][1] + "," + coords[0][0] + " " + coords[1][1] + "," + coords[1][0])) {
					bw.write("" + coords[0][1] + "," + coords[0][0] + " " + coords[1][1] + "," + coords[1][0]);
					bw.newLine();
				}
			} else {
				fw = new FileWriter(playData, !playDataExists);
				bw = new BufferedWriter(fw);
				for (double[] row : playerEvalFunc) {
					for (double eval : row) {
						bw.write(String.valueOf(eval) + "\t\t");
					}
					bw.newLine();
				}

				int[][] coords = getCoordsFromSoln(soln);
				if (!movesSoFar
						.contains("" + coords[0][1] + "," + coords[0][0] + " " + coords[1][1] + "," + coords[1][0])) {
					bw.write("" + coords[0][1] + "," + coords[0][0] + " " + coords[1][1] + "," + coords[1][0]);
				}
				bw.newLine();
			}

			bw.close();
			fw.close();

			long endTime = System.currentTimeMillis();
			float time = (endTime - startTime + 0.0f) / 1000;
			bufferedWriter.write("Remaining time - " + (remainingTime - time));
			bufferedWriter.newLine();
			System.out.println("That took " + (endTime - startTime) + " milliseconds");
			bufferedWriter.close();
			writer.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String[] solveUsingMinMax(Queue<String> input) {
		gameType = input.poll();
		boolean isWhiteType = input.poll().equalsIgnoreCase("white");
		playerType = isWhiteType;
		remainingTime = Float.parseFloat(input.poll()); // Find better way to add pruning
		calculateMoveTimeLimit(); // For time based pruning
		depth = validSearchDepthMinMax(remainingTime);
		int idx = 0;

		// Player
		HashMap<String, BoardPiece> campPieces = new HashMap<String, BoardPiece>();
		HashMap<String, BoardPiece> pieces = new HashMap<String, BoardPiece>();
		List<String> campLoc = isWhiteType ? Arrays.asList(whiteCampLoc) : Arrays.asList(blackCampLoc);
		List<String> goalCamp = isWhiteType ? Arrays.asList(blackCampLoc) : Arrays.asList(whiteCampLoc);

		// Opponent
		HashMap<String, BoardPiece> oppCampPieces = new HashMap<String, BoardPiece>();
		HashMap<String, BoardPiece> oppPieces = new HashMap<String, BoardPiece>();
		List<String> oppCampLoc = !isWhiteType ? Arrays.asList(whiteCampLoc) : Arrays.asList(blackCampLoc);
		List<String> oppGoalCampLoc = isWhiteType ? Arrays.asList(whiteCampLoc) : Arrays.asList(blackCampLoc);

		generateEvalFuncMinMax(campLoc, goalCamp, isWhiteType);
		int count = 0, oppCount = 0;
		while (!input.isEmpty() && idx < 16) {
			char[] row = input.poll().toCharArray();
			for (int i = 0; i < row.length; i++) {
				gameBoard[idx][i] = row[i];
				if (gameBoard[idx][i] == (isWhiteType ? 'W' : 'B')) {
					if (campLoc.contains("" + i + "," + idx))
						campPieces.put("" + i + "," + idx,
								new BoardPiece(idx, i, playerEvalFunc[idx][i], new ArrayList<String>(), 'N'));
					else
						pieces.put("" + i + "," + idx,
								new BoardPiece(idx, i, playerEvalFunc[idx][i], new ArrayList<String>(), 'N'));
					if (goalCamp.contains("" + i + "," + idx))
						count++;
				} else if (gameBoard[idx][i] == (!isWhiteType ? 'W' : 'B')) {
					if (oppCampLoc.contains("" + i + "," + idx))
						oppCampPieces.put("" + i + "," + idx,
								new BoardPiece(idx, i, opponentEvalFunc[idx][i], new ArrayList<String>(), 'N'));
					else
						oppPieces.put("" + i + "," + idx,
								new BoardPiece(idx, i, opponentEvalFunc[idx][i], new ArrayList<String>(), 'N'));
					if (oppGoalCampLoc.contains("" + i + "," + idx))
						oppCount++;
				}
			}
			idx++;
		}

//		if(count > 13) depth = 0; //For faster convergence, can be debated

		PlayerMetadata player = new PlayerMetadata();
		player.campPieces = campPieces;
		player.pieces = pieces;
		player.goalPieces = count;

		PlayerMetadata opponent = new PlayerMetadata();
		opponent.campPieces = oppCampPieces;
		opponent.pieces = oppPieces;
		opponent.goalPieces = oppCount;

		MinMaxMetadata sol = maxAgent(gameBoard, isWhiteType, 0, player, opponent, new MinMaxMetadata());
		return sol.soln;
	}

	public static void generateEvalFuncMinMax(List<String> homeCamp, List<String> goalCamp, boolean playerType) {
		if (!playDataExists) {
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 16; j++) {
					String loc = "" + j + "," + i + "";
					if (homeCamp.contains(loc)) {
						if (!playerType)
							playerEvalFunc[i][j] = 1000 - i - j;
						else
							playerEvalFunc[i][j] = 1000 + i + j - 30;
					} else if (goalCamp.contains(loc)) {
						if (playerType)
							playerEvalFunc[i][j] = (i * i + j * j + 0.0f) / (17 * 17);
						else
							playerEvalFunc[i][j] = (((15 - i) * (15 - i)) + ((15 - j) * (15 - j)) + 0.0f) / (17 * 17);
					} else {
						for (String coord : goalCamp) {
							int row = Integer.parseInt(coord.split(",")[1]);
							int col = Integer.parseInt(coord.split(",")[0]);
							playerEvalFunc[i][j] = Float.MIN_VALUE;
							playerEvalFunc[i][j] = Math.max(Math.pow(i - row, 2) + Math.pow(j - col, 2), playerEvalFunc[i][j]);
						}
						//playerEvalFunc[i][j] = playerEvalFunc[i][j] / goalCamp.size(); // Average of values. Change to
																						// better alternative
					}
				}
			}
		}
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				opponentEvalFunc[i][j] = playerEvalFunc[15 - i][15 - j];
			}
		}
	}

	public static MinMaxMetadata maxAgent(char[][] gameBoard, boolean playerType, int currentDepth,
			PlayerMetadata player, PlayerMetadata opponent, MinMaxMetadata alphaBeta) {
		List<BoardPiece> moves = generateMoves(player, playerType, gameBoard);
		List<String> homeCamp = Arrays.asList(playerType ? whiteCampLoc : blackCampLoc);

		if (!player.campPieces.isEmpty()) {
			MinMaxMetadata sol = new MinMaxMetadata();
			Collections.sort(moves, new Comparator<BoardPiece>() {
				@Override
				public int compare(BoardPiece p1, BoardPiece p2) {
					if (p1.evalValue < p2.evalValue)
						return -1;
					else if (p1.evalValue > p2.evalValue)
						return 1;
					return 0;
				}
			});
			sol.soln = moves.get(0).moves.toArray(new String[moves.get(0).moves.size()]);
			return sol;
		}
		
		Collections.sort(moves, new Comparator<BoardPiece>() {
			@Override
			public int compare(BoardPiece p1, BoardPiece p2) {
				if (p1.parent.evalValue < p2.parent.evalValue)
					return 1;
				else if (p1.parent.evalValue > p2.parent.evalValue)
					return -1;

				// Equal evalvalue for parent
				if (p1.evalValue < p2.evalValue)
					return -1;
				else if (p1.evalValue > p2.evalValue)
					return 1;
				else
					return 0;
			}
		});

		MinMaxMetadata evalObj = new MinMaxMetadata();
		evalObj.alpha = alphaBeta.alpha;
		evalObj.beta = alphaBeta.beta;
		evalObj.evalValue = alphaBeta.evalValue;
		evalObj.soln = alphaBeta.soln;
		BoardPiece bestMove = moves.isEmpty() ? null : moves.get(0);
		if (currentDepth < depth) {
			double evalValue = Integer.MIN_VALUE;
			for (BoardPiece move : moves) {
				long endTime = System.currentTimeMillis();
				float time = (endTime - startTime + 0.0f) / 1000;
				if (moveTimeLimit < time) {
					System.out.println("Time Exceeded");
					return evalObj; // If move exceeds time, return best move so far
				}
				int[][] coords;
				if (move.parent == null) {
					coords = getCoords(move, player);
				} else {
					coords = new int[2][2];
					coords[1][0] = move.row;
					coords[1][1] = move.col;
					coords[0][0] = move.parent.row;
					coords[0][1] = move.parent.col;
				}

				if (move.evalValue > move.parent.evalValue && player.goalPieces < 12) {
					continue;
				}

				if (movesSoFar
						.contains("" + coords[1][1] + "," + coords[1][0] + " " + coords[0][1] + "," + coords[0][0])) {
					continue;
				}

				char temp = gameBoard[coords[0][0]][coords[0][1]];
				gameBoard[coords[0][0]][coords[0][1]] = gameBoard[coords[1][0]][coords[1][1]];
				gameBoard[coords[1][0]][coords[1][1]] = temp;

				BoardPiece parent;
				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					parent = player.campPieces.remove("" + coords[0][1] + "," + coords[0][0]);
				} else {
					parent = player.pieces.remove("" + coords[0][1] + "," + coords[0][0]);
				}

				double beta = evalObj.beta;
				double alpha = evalObj.alpha;

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.put("" + coords[1][1] + "," + coords[1][0], move);
				} else {
					player.pieces.put("" + coords[1][1] + "," + coords[1][0], move);
				}

				MinMaxMetadata min = minAgent(gameBoard, !playerType, currentDepth + 1, opponent, player, evalObj);
				evalObj.evalValue = Math.max(evalValue, min.evalValue);

				if (currentDepth == 0 && evalObj.evalValue == min.evalValue && min.evalValue >= evalValue) {
					if (min.evalValue > evalValue) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						bestMove = move;
					} else if (min.evalValue == evalValue && bestMove.moveDistance < move.moveDistance) {
						bestMove = move;
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // Try to select the farthest
																							// jump
																							// rather than just the
																							// first best
					} else if (min.evalValue == evalValue && bestMove.moveDistance == move.moveDistance) {
						if (bestMove.evalValue > move.evalValue) {
							bestMove = move;
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						}
					}
				}

				evalValue = evalObj.evalValue;

				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					player.campPieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				} else {
					player.pieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				}

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.remove("" + coords[1][1] + "," + coords[1][0]);
				} else {
					player.pieces.remove("" + coords[1][1] + "," + coords[1][0]);
				}

				temp = gameBoard[coords[0][0]][coords[0][1]];
				gameBoard[coords[0][0]][coords[0][1]] = gameBoard[coords[1][0]][coords[1][1]];
				gameBoard[coords[1][0]][coords[1][1]] = temp;

				if (evalObj.evalValue >= beta) {
					if (currentDepth == 0) {
						if (evalObj.evalValue == beta) {
							if (evalObj.soln.length < move.moves.size())
								evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						} else
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
					}
					if (currentDepth != 0)
						return evalObj; // Check once
				}

				if (evalObj.evalValue > alpha) {
					if (currentDepth == 0)
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
				}

				if (evalObj.evalValue == alpha) {
					if (currentDepth == 0 && evalObj.soln.length < move.moves.size())
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
				}

				evalObj.alpha = Math.max(alpha, evalObj.evalValue);
			}
			return evalObj;
		} else {
			// Calculate Terminal Eval Value
			double maxEval = Integer.MIN_VALUE;
			double[][] evalFunc = playerEvalFunc;
			double[][] oppEvalFunc = opponentEvalFunc;
			for (BoardPiece move : moves) {
				long endTime = System.currentTimeMillis();
				float time = (endTime - startTime + 0.0f) / 1000;
				if (moveTimeLimit < time) {
					System.out.println("Time Exceeded");
					break; // If move exceeds time, return best move so far
				}
				int[][] coords;
				if (move.parent == null) {
					coords = getCoords(move, player);
				} else {
					coords = new int[2][2];
					coords[1][0] = move.row;
					coords[1][1] = move.col;
					coords[0][0] = move.parent.row;
					coords[0][1] = move.parent.col;
				}

				if (move.evalValue > move.parent.evalValue && player.goalPieces < 12) {
					continue;
				}

				if (movesSoFar
						.contains("" + coords[1][1] + "," + coords[1][0] + " " + coords[0][1] + "," + coords[0][0]))
					continue;

				BoardPiece parent;
				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					parent = player.campPieces.remove("" + coords[0][1] + "," + coords[0][0]);
				} else {
					parent = player.pieces.remove("" + coords[0][1] + "," + coords[0][0]);
				}
				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.put("" + coords[1][1] + "," + coords[1][0], move);
				} else {
					player.pieces.put("" + coords[1][1] + "," + coords[1][0], move);
				}

				double eval = 20000; // Base value for Eval
				if (player.campPieces.size() > 0) { // Player piece in home camp, should not occur
					eval = -50000;
				} else {
					// Calculate Eval Value
					for (BoardPiece piece : player.pieces.values().toArray(new BoardPiece[player.pieces.size()])) {
						eval = eval - evalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : player.campPieces.values()) {
						eval = eval - evalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : opponent.pieces.values()) {
						eval = eval + oppEvalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : opponent.campPieces.values()) {
						eval = eval + oppEvalFunc[piece.row][piece.col];
					}
				}

				if (maxEval <= eval && currentDepth == 0) {
					if (maxEval < eval) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
						bestMove = move;
					} else if (maxEval == eval && bestMove.moveDistance < move.moveDistance) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
						bestMove = move;
					} else if (maxEval == eval && bestMove.moveDistance == move.moveDistance) {
						if (bestMove.evalValue > move.evalValue) {
							bestMove = move;
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						}
					}
				}

				maxEval = Math.max(maxEval, eval);

				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					player.campPieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				} else {
					player.pieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				}

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.remove("" + coords[1][1] + "," + coords[1][0]);
				} else {
					player.pieces.remove("" + coords[1][1] + "," + coords[1][0]);
				}


				if (maxEval > evalObj.beta) {
					MinMaxMetadata res = new MinMaxMetadata();
					res.alpha = evalObj.alpha;
					res.beta = evalObj.beta;
					res.evalValue = maxEval;
					if (currentDepth == 0)
						res.soln = move.moves.toArray(new String[move.moves.size()]);
					if (currentDepth != 0)
						return res;
				}

				if (maxEval == evalObj.beta) {
					if (currentDepth == 0 && evalObj.soln.length < move.moves.size())
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
				}
			}

			MinMaxMetadata res = new MinMaxMetadata();
			res.alpha = evalObj.alpha;
			res.beta = evalObj.beta;
			res.evalValue = maxEval;
			if (currentDepth == 0)
				res.soln = evalObj.soln;
			if (moves.isEmpty()) { // Case when at depth>0, there's no more possible moves to make
				double eval = 20000;
				for (BoardPiece piece : player.pieces.values().toArray(new BoardPiece[player.pieces.size()])) {
					eval = eval - evalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : player.campPieces.values()) {
					eval = eval - evalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : opponent.pieces.values()) {
					eval = eval + oppEvalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : opponent.campPieces.values()) {
					eval = eval + oppEvalFunc[piece.row][piece.col];
				}
				res.evalValue = eval;
			}
			return res;
		}
	}

	public static MinMaxMetadata minAgent(char[][] gameBoard, boolean playerType, int currentDepth,
			PlayerMetadata player, PlayerMetadata opponent, MinMaxMetadata alphaBeta) {
		List<BoardPiece> moves = generateMoves(player, playerType, gameBoard);
		List<String> homeCamp = Arrays.asList(playerType ? whiteCampLoc : blackCampLoc);
		Collections.sort(moves, new Comparator<BoardPiece>() {
			@Override
			public int compare(BoardPiece p1, BoardPiece p2) {
				if (p1.parent.evalValue < p2.parent.evalValue)
					return 1;
				else if (p1.parent.evalValue > p2.parent.evalValue)
					return -1;

				// Equal evalvalue for parent
				if (p1.evalValue < p2.evalValue)
					return -1;
				else if (p1.evalValue > p2.evalValue)
					return 1;
				else
					return 0;
			}
		});

		MinMaxMetadata evalObj = new MinMaxMetadata();
		evalObj.alpha = alphaBeta.alpha;
		evalObj.beta = alphaBeta.beta;
		evalObj.evalValue = alphaBeta.evalValue;
		evalObj.soln = alphaBeta.soln;
		BoardPiece bestMove = moves.isEmpty() ? null : moves.get(0);
		if (currentDepth < depth) {
			double evalValue = Integer.MAX_VALUE;
			for (BoardPiece move : moves) {
				long endTime = System.currentTimeMillis();
				float time = (endTime - startTime + 0.0f) / 1000;
				if (moveTimeLimit < time) {
					System.out.println("Time Exceeded");
					return evalObj; // If move exceeds time, return best move so far
				}
				int[][] coords;
				if (move.parent == null) {
					coords = getCoords(move, player);
				} else {
					coords = new int[2][2];
					coords[1][0] = move.row;
					coords[1][1] = move.col;
					coords[0][0] = move.parent.row;
					coords[0][1] = move.parent.col;
				}

				if (move.evalValue > move.parent.evalValue && player.goalPieces < 12)
					continue;

				char temp = gameBoard[coords[0][0]][coords[0][1]];
				gameBoard[coords[0][0]][coords[0][1]] = gameBoard[coords[1][0]][coords[1][1]];
				gameBoard[coords[1][0]][coords[1][1]] = temp;

				BoardPiece parent;
				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					parent = player.campPieces.remove("" + coords[0][1] + "," + coords[0][0]);
				} else {
					parent = player.pieces.remove("" + coords[0][1] + "," + coords[0][0]);
				}

				double beta = evalObj.beta;
				double alpha = evalObj.alpha;

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.put("" + coords[1][1] + "," + coords[1][0], move);
				} else {
					player.pieces.put("" + coords[1][1] + "," + coords[1][0], move);
				}

				MinMaxMetadata max = maxAgent(gameBoard, !playerType, currentDepth + 1, opponent, player, evalObj);
				evalObj.evalValue = Math.min(evalValue, max.evalValue);

				if (currentDepth == 0 && evalValue >= max.evalValue) {
					if (max.evalValue < evalValue) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						bestMove = move;
					} else if (max.evalValue == evalValue && bestMove.moveDistance < move.moveDistance) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						bestMove = move;
					} else if (max.evalValue == evalValue && bestMove.moveDistance == move.moveDistance) {
						if (bestMove.evalValue > move.evalValue) {
							bestMove = move;
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						}
					}
				}

				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					player.campPieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				} else {
					player.pieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				}

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.remove("" + coords[1][1] + "," + coords[1][0]);
				} else {
					player.pieces.remove("" + coords[1][1] + "," + coords[1][0]);
				}

				temp = gameBoard[coords[0][0]][coords[0][1]];
				gameBoard[coords[0][0]][coords[0][1]] = gameBoard[coords[1][0]][coords[1][1]];
				gameBoard[coords[1][0]][coords[1][1]] = temp;

				evalValue = evalObj.evalValue;
				if (evalObj.evalValue <= alpha) {
					if (currentDepth == 0) {
						if (evalObj.evalValue == alpha && evalObj.soln.length < move.moves.size())
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						else if (evalObj.evalValue < alpha)
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
					}
					return evalObj;
				}

				evalObj.beta = Math.min(beta, evalObj.evalValue);
				if (evalObj.beta == evalValue && currentDepth == 0) {
					if (evalObj.beta < beta)
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
					else if (beta == evalValue && evalObj.soln.length < move.moves.size()) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
					}
				}
			}
			return evalObj;
		} else {
			// Calculate Terminal Eval Value
			double minEval = Integer.MAX_VALUE;
			double[][] evalFunc = opponentEvalFunc;
			double[][] oppEvalFunc = playerEvalFunc;
			for (BoardPiece move : moves) {
				long endTime = System.currentTimeMillis();
				float time = (endTime - startTime + 0.0f) / 1000;
				if (moveTimeLimit < time) {
					System.out.println("Time Exceeded");
					break; // If move exceeds time, return best move so far
				}
				int[][] coords;
				if (move.parent == null) {
					coords = getCoords(move, player);
				} else {
					coords = new int[2][2];
					coords[1][0] = move.row;
					coords[1][1] = move.col;
					coords[0][0] = move.parent.row;
					coords[0][1] = move.parent.col;
				}
				if (move.evalValue > move.parent.evalValue && player.goalPieces < 12) {
					continue;
				}
				BoardPiece parent;
				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					parent = player.campPieces.remove("" + coords[0][1] + "," + coords[0][0]);
				} else {
					parent = player.pieces.remove("" + coords[0][1] + "," + coords[0][0]);
				}
				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.put("" + coords[1][1] + "," + coords[1][0], move);
				} else {
					player.pieces.put("" + coords[1][1] + "," + coords[1][0], move);
				}

				double eval = 1000; // Base value for Eval
				if (player.campPieces.size() > 0) { // Player piece in home camp, should not occur
					eval = 50000;
				} else {
					eval = 20000;
					// Calculate Eval Value
					for (BoardPiece piece : player.pieces.values()) {
						eval = eval + evalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : player.campPieces.values()) {
						eval = eval + evalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : opponent.pieces.values()) {
						eval = eval - oppEvalFunc[piece.row][piece.col];
					}
					for (BoardPiece piece : opponent.campPieces.values()) {
						eval = eval - oppEvalFunc[piece.row][piece.col];
					}
				}

				if (currentDepth == 0 && minEval >= eval) {
					if (minEval > eval) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
						bestMove = move;
					} else if (minEval == eval && bestMove.moveDistance < move.moveDistance) {
						evalObj.soln = move.moves.toArray(new String[move.moves.size()]); // To store the optimal value
						bestMove = move;
					} else if (minEval == eval && bestMove.moveDistance == move.moveDistance) {
						if (bestMove.evalValue > move.evalValue) {
							bestMove = move;
							evalObj.soln = move.moves.toArray(new String[move.moves.size()]);
						}
					}
				}

				minEval = Math.min(minEval, eval);

				if (homeCamp.contains("" + coords[0][1] + "," + coords[0][0])) {
					player.campPieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				} else {
					player.pieces.put("" + coords[0][1] + "," + coords[0][0], parent);
				}

				if (homeCamp.contains("" + coords[1][1] + "," + coords[1][0])) {
					player.campPieces.remove("" + coords[1][1] + "," + coords[1][0]);
				} else {
					player.pieces.remove("" + coords[1][1] + "," + coords[1][0]);
				}

				if (minEval <= evalObj.alpha) {
					MinMaxMetadata res = new MinMaxMetadata();
					res.alpha = evalObj.alpha;
					res.beta = evalObj.beta;
					res.evalValue = minEval;
					if (currentDepth == 0)
						res.soln = move.moves.toArray(new String[move.moves.size()]);
					if (currentDepth != 0)
						return res;
				}
			}

			MinMaxMetadata res = new MinMaxMetadata();
			res.alpha = evalObj.alpha;
			res.beta = evalObj.beta;
			res.evalValue = minEval;
			if (currentDepth == 0)
				res.soln = evalObj.soln;
			if (moves.isEmpty()) { // Case when at depth>0, there's no more possible moves to make
				double eval = 20000;
				// Calculate Eval Value
				for (BoardPiece piece : player.pieces.values()) {
					eval = eval + evalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : player.campPieces.values()) {
					eval = eval + evalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : opponent.pieces.values()) {
					eval = eval - oppEvalFunc[piece.row][piece.col];
				}
				for (BoardPiece piece : opponent.campPieces.values()) {
					eval = eval - oppEvalFunc[piece.row][piece.col];
				}
				res.evalValue = eval;
			}
			return res;
		}
	}

	public static int[][] getCoords(BoardPiece move, PlayerMetadata player) {
		int[][] pts = new int[2][2];
		for (int idx = 0; idx < move.moves.size(); idx++) {
			String[] coords = move.moves.get(idx).split(" ");
			int to_row = Integer.parseInt(coords[1].split(",")[1]);
			int to_col = Integer.parseInt(coords[1].split(",")[0]);
			if (player.pieces.containsKey("" + to_col + "," + to_row)
					|| player.campPieces.containsKey("" + to_col + "," + to_row)) { // check if correct
				pts[0][0] = to_row;
				pts[0][1] = to_col;
			}
			pts[1][0] = Integer.parseInt(coords[2].split(",")[1]);
			pts[1][1] = Integer.parseInt(coords[2].split(",")[0]);
		}
		return pts;
	}

	public static int[][] getCoordsFromSoln(String[] moves) {
		int[][] pts = new int[2][2];
		for (int idx = 0; idx < moves.length; idx++) {
			String[] coords = moves[idx].split(" ");
			int to_row = Integer.parseInt(coords[1].split(",")[1]);
			int to_col = Integer.parseInt(coords[1].split(",")[0]);
			if (idx == 0) { // check if correct
				pts[0][0] = to_row;
				pts[0][1] = to_col;
			}
			pts[1][0] = Integer.parseInt(coords[2].split(",")[1]);
			pts[1][1] = Integer.parseInt(coords[2].split(",")[0]);
		}
		return pts;
	}

	public static List<BoardPiece> generateMoves(PlayerMetadata player, boolean playerType, char[][] gameBoard) {
		HashMap<String, BoardPiece> pieces;
		HashMap<String, BoardPiece> remPieces;
		if (!player.campPieces.isEmpty()) {
			pieces = player.campPieces;
			remPieces = player.pieces;
		} else if (!player.pieces.isEmpty()) {
			pieces = player.pieces;
			remPieces = player.campPieces;
		} else
			return null;

		List<BoardPiece> moves = new ArrayList<BoardPiece>();
		for (BoardPiece piece : pieces.values()) {
			List<BoardPiece> singleMoves = findValidNeighborMovesMinMax(piece, playerType, player.goalPieces);
			singleMoves.addAll(findValidJumpMovesMinMax(piece, gameBoard, new ArrayList<BoardPiece>(), playerType, 0));
			moves.addAll(singleMoves);
		}

		if (moves.isEmpty() && player.goalPieces != 16) { // When no moves can be made in homeCamp, generate moves for
															// outer pieces
			for (BoardPiece piece : remPieces.values()) {
				List<BoardPiece> singleMoves = findValidNeighborMovesMinMax(piece, playerType, player.goalPieces);
				singleMoves
						.addAll(findValidJumpMovesMinMax(piece, gameBoard, new ArrayList<BoardPiece>(), playerType, 0));
				moves.addAll(singleMoves);
			}
		}

		return moves;
	}

	public static List<BoardPiece> findValidNeighborMovesMinMax(BoardPiece piece, boolean playerType,
			int goalPieceCount) {
		double[][] evalFunc = playerType == homework.playerType ? playerEvalFunc : opponentEvalFunc;
		List<String> goalCamp = playerType ? Arrays.asList(blackCampLoc) : Arrays.asList(whiteCampLoc);

		List<BoardPiece> moves = new ArrayList<BoardPiece>();
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (piece.row + i < 0 || piece.row + i >= 16)
					continue;
				if (piece.col + j < 0 || piece.col + j >= 16)
					continue;
				if (i == 0 && j == 0)
					continue;
				if (goalPieceCount < 11 && piece.evalValue <= evalFunc[piece.row + i][piece.col + j]) // To prune moves
																										// with worse
																										// configurations
					continue;
				if (goalPieceCount >= 11 && goalCamp.contains("" + piece.col + "," + piece.row)
						&& goalCamp.contains("" + (piece.col + j) + "," + (piece.row + i))
						&& piece.evalValue <= evalFunc[piece.row + i][piece.col + j])
					continue;
				if (goalPieceCount >= 11 && goalCamp.contains("" + piece.col + "," + piece.row)
						&& !goalCamp.contains("" + (piece.col + j) + "," + (piece.row + i)))
					continue;

				if (gameBoard[piece.row + i][piece.col + j] != '.')
					continue;
				List<String> move = new ArrayList<>();
				move.add("E " + piece.col + "," + piece.row + " " + (piece.col + j) + "," + (piece.row + i));
				BoardPiece p = new BoardPiece(piece.row + i, piece.col + j, evalFunc[piece.row + i][piece.col + j],
						move, 'E');
				p.parent = piece;
				p.moveDistance = Math.pow(Math.abs(p.row - piece.row),2) + Math.pow(Math.abs(p.col - piece.col),2);
				moves.add(p);
			}
		}
		return moves;
	}

	public static List<BoardPiece> findValidJumpMovesMinMax(BoardPiece piece, char[][] gameBoard,
			List<BoardPiece> parents, boolean playerType, int level) {
		List<BoardPiece> moves = new ArrayList<BoardPiece>();
		List<String> homeCamp = Arrays.asList(playerType ? whiteCampLoc : blackCampLoc);
		List<String> goalCamp = Arrays.asList(playerType ? blackCampLoc : whiteCampLoc);
		double[][] evalFunc = playerType == homework.playerType ? playerEvalFunc : opponentEvalFunc;

		for (int i = -2; i <= 2; i += 2) {
			for (int j = -2; j <= 2; j += 2) {
				if (piece.row + i < 0 || piece.row + i >= 16)
					continue;
				if (piece.col + j < 0 || piece.col + j >= 16)
					continue;
				if (i == 0 && j == 0)
					continue;
				if (gameBoard[piece.row + i / 2][piece.col + j / 2] == '.')
					continue;
				if (gameBoard[piece.row + i][piece.col + j] != '.')
					continue;
				if (!homeCamp.contains("" + piece.col + "," + piece.row)
						&& homeCamp.contains("" + (piece.col + j) + "," + (piece.row + i)))
					continue;
				List<String> move = new ArrayList<>();
				move.addAll(piece.moves);
				move.add("J " + piece.col + "," + piece.row + " " + (piece.col + j) + "," + (piece.row + i));
				BoardPiece jump = new BoardPiece(piece.row + i, piece.col + j, evalFunc[piece.row + i][piece.col + j],
						move, 'J');
				if (parents.contains(jump))
					continue;
				if (goalCamp.contains("" + piece.col + "," + piece.row)
						&& goalCamp.contains("" + jump.col + "," + jump.row) && jump.evalValue >= piece.evalValue)
					continue; // Only keep valid moves inside goalCamp which decrease eval value
				if (!goalCamp.contains("" + jump.col + "," + jump.row)
						&& goalCamp.contains("" + piece.col + "," + piece.row))
					continue; // Only keep valid moves, invalid since moving out of goal camp
				if (jump.evalValue <= piece.evalValue)
					moves.add(jump);
				if (level + 1 > validSearchDepth(remainingTime))
					continue; // Time dependent depth search
				parents.add(piece);
				moves.addAll(findValidJumpMovesMinMax(jump, gameBoard, parents, playerType, level + 1));
				parents.remove(piece);
			}
		}
		if (level == 0) {
			for (BoardPiece p : moves) {
				p.parent = piece;
				p.moveDistance = Math.pow(Math.abs(p.row - piece.row), 2) + Math.pow(Math.abs(p.col - piece.col), 2);
			}
		}
		return moves;
	}

	public static int validSearchDepthMinMax(float remainingTime) { // Search depth
		if (gameType.equalsIgnoreCase("single"))
			return 2;
		if (remainingTime > 230)
			return 3;
		if (remainingTime > 14)
			return 2;
		return 0;
	}

	public static int validSearchDepth(float remainingTime) { // Jump Depth
		if (gameType.equalsIgnoreCase("single"))
			return 15;
		if (remainingTime > 85)
			return 25;
		if (remainingTime > 20)
			return 18;
		return 12;
	}

	public static void calculateMoveTimeLimit() {
		if (gameType.equalsIgnoreCase("single"))
			moveTimeLimit = remainingTime*0.9f;
		if (remainingTime > 200f)
			moveTimeLimit = remainingTime*0.15f;
		else if (remainingTime > 50f)
			moveTimeLimit = remainingTime*0.12f;
		else if (remainingTime > 30f)
			moveTimeLimit = 5f;
		else if (remainingTime > 10f)
			moveTimeLimit = 0.15f * remainingTime;
		else
			moveTimeLimit = 1.5f;
	}
}

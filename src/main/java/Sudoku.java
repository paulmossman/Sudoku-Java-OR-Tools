
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;

public class Sudoku {

	public static void main(String[] args) throws Exception {

		//@formatter:off
		int[][] blank_9x9 = new int[][] {
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0 }
		};

		// https://www.sudokuwiki.org Weekly Extreme for Feb 12-18, 2022
		int[][] extereme_9x9 = new int[][] {
			{ 0, 0, 0,/**/0, 0, 7,/**/0, 0, 9 },
			{ 0, 0, 0,/**/0, 8, 0,/**/0, 6, 0 },
			{ 4, 0, 0,/**/2, 0, 0,/**/5, 0, 0 },
			/*-------------------------------*/
			{ 0, 0, 0,/**/0, 0, 9,/**/0, 0, 8 },
			{ 0, 0, 0,/**/0, 6, 0,/**/0, 7, 0 },
			{ 0, 3, 0,/**/1, 0, 0,/**/2, 0, 0 },
			/*-------------------------------*/
			{ 0, 1, 6,/**/4, 0, 0,/**/3, 0, 0 },
			{ 5, 4, 3,/**/0, 0, 0,/**/0, 0, 0 },
			{ 2, 0, 0,/**/0, 0, 0,/**/0, 0, 0 }
		};
		//formatter:on

		print(solve(extereme_9x9));
	}

	public static int[][] solve(int[][] sudoku_9x9) throws Exception {

		Loader.loadNativeLibraries();
		CpModel model = new CpModel();

		// One IntVar per cell.
		IntVar intvar_sudoku_9x9[][] = new IntVar[9][9];
		for (int row = 0; row < 9; row++) {
			for (int column = 0; column < 9; column++) {

				// Blank or known?
				if (sudoku_9x9[row][column] == 0) {
					// Blank, so needs to be solved.
					intvar_sudoku_9x9[row][column] = model.newIntVar(1, 9, "solve cell[" + row + "][" + column + "]");
				} else if (sudoku_9x9[row][column] >= 1 && sudoku_9x9[row][column] <= 9) {
					// Known, so a constant.
					intvar_sudoku_9x9[row][column] = model.newConstant(sudoku_9x9[row][column]);
				} else {
					throw new Exception("Invalid suduku cell value: " + sudoku_9x9[row][column]);
				}
			}
		}

		// Column constraints.
		for (int row = 0; row < 9; row++) {
			model.addAllDifferent(intvar_sudoku_9x9[row]);
		}

		// Row constraints.
		for (int column = 0; column < 9; column++) {
			IntVar rowcells[] = new IntVar[9];
			for (int row = 0; row < 9; row++) {
				rowcells[row] = intvar_sudoku_9x9[row][column];
			}
			model.addAllDifferent(rowcells);
		}

		// 3x3 subgrid constraints.
		for (int row = 0; row < 9; row += 3) {
			for (int column = 0; column < 9; column += 3) {
				IntVar subgrid[] = new IntVar[9];
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						subgrid[i * 3 + j] = intvar_sudoku_9x9[row + i][column + j];
					}
				}
				model.addAllDifferent(subgrid);
			}
		}

		// Solve
		CpSolver solver = new CpSolver();
		solver.getParameters().setMaxTimeInSeconds(60);
		CpSolverStatus status = solver.solve(model);

		// Check the solution
		System.out.println();
		System.out.println(String.format("CpSolverStatus: %s  solve time: %.4fs", status, solver.wallTime()));
		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

			// Extract the solution.
			for (int row = 0; row < 9; row++) {
				for (int column = 0; column < 9; column++) {
					sudoku_9x9[row][column] = (int) solver.value(intvar_sudoku_9x9[row][column]);
				}
			}
		}
		else {
			throw new Exception("CpSolverStatus: " + status);
		}

		return sudoku_9x9;
	}

	public static void print(int[][] sudoku_9x9) {
		for (int row = 0; row < sudoku_9x9[0].length; row++) {
			if (row != 0 && row % 3 == 0) {
				System.out.println("---------------------");
			}
			for (int column = 0; column < sudoku_9x9[row].length; column++) {
				if (column != 0 && column % 3 == 0) {
					System.out.print("| ");
				}
				System.out.print(sudoku_9x9[row][column] + " ");
			}
			System.out.println();
		}
	}

}

package pgv.rendevous;

import java.util.ArrayList;

public class Main {

	private static int nPlayers =4;

	private static Player players[];

	private static ArrayList<Integer> cards = new ArrayList<Integer>();

	public static void main(String[] args) {

		for (int i = 0; i < nPlayers; i++) {
			for (int j = 1; j <= 4; j++)
				cards.add(i);
		}

		players = new Player[nPlayers];

		for (int i = 0; i < players.length; i++) {
			players[i] = new Player(reparteCartas(), players, i);
			players[i].start();
		}
		
	}

	private static ArrayList<Integer> reparteCartas() {
		ArrayList<Integer> dealCards = new ArrayList<Integer>();
		int index = (int) (Math.random() * cards.size());
		for (int i = 0; i < 4; i++) {
			dealCards.add(cards.get(index));

			cards.remove(index);

			index = (int) (Math.random() * cards.size());

		}

		return dealCards;

	}

}

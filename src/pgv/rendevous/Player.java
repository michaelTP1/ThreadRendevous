package pgv.rendevous;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Player extends Thread {

	private static Semaphore barrera1 = new Semaphore(0);
	private static Semaphore barrera2 = new Semaphore(1);
	private static Semaphore barrera3 = new Semaphore(1);
	private static Semaphore mutex = new Semaphore(1);

	private static int won = 0, preparados, hadWon = -1;

	private static Player players[];
	private int index;

	private boolean allEquals = false;

	private int discard, newCard;

	ArrayList<Integer> currentCards = new ArrayList<Integer>();

	@Override
	public void run() {
		super.run();

		try {
			while (won == 0) {

				mutex.acquire();

				{ // REGION CRITICA MUTEX

					preparados++;
					allEquals = sort();
					System.out.println(this.getName() + " " + currentCards.toString());
					if (preparados == players.length) {
						System.out.println();
						// ABRIR BARRERA 1 y cerrar barrera 2 y 3

						barrera2.acquire();
						barrera3.acquire();
						barrera1.release();

					}
				}
				mutex.release();

				barrera1.acquire();
				barrera1.release();

				mutex.acquire();
				{
					preparados--;

					seleccionarDescarte();
					if (preparados == 0) {

						// ABRIR BARRERA 2 y cerrar barrera 1

						barrera1.acquire();
						barrera2.release();

					}

				}
				mutex.release();
				barrera2.acquire();
				barrera2.release();

				// *************************************************************
				// ************* PUNTO CRITICO (rendez vous) *******************

				mutex.acquire();
				{
					preparados++;
					if (won == 0) {

						if (allEquals) {

							won++;
							hadWon = index;

						}
					}

					if (index != players.length - 1)
						newCard = players[index + 1].getDiscard();
					else
						newCard = players[0].getDiscard();
					cambiaCarta();
					if (preparados == players.length) {
						preparados = 0;
						barrera3.release();

					}

				}
				mutex.release();
				barrera3.acquire();
				barrera3.release();

				// *************************************************************
				// ************* PUNTO CRITICO (rendez vous) *******************

				mutex.acquire();
				{
					if (hadWon != index) {

						if (won != 0) {

							won++;
							if (won == players.length)
								System.out.println(this.getName() + " he perdido");
							else
								System.out.println(getName() + " he ganado");
						}
					} else {
						System.out.println(getName() + " he ganado");
					}
				}
				mutex.release();

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// FUNCIONES

	public Player(ArrayList<Integer> cards, Player[] players, int index) {

		Player.players = players;

		this.index = index;

		currentCards = cards;

	}

	// ORDENA LAS CARTAS

	private boolean sort() {
		ArrayList<Integer> auxCurrentCards = new ArrayList<Integer>();
		boolean found = false;

		for (int i = 0; i < currentCards.size() - 1; i++) {

			found = false;

			for (int j = i + 1; j < currentCards.size(); j++) {
				if (currentCards.get(i) == currentCards.get(j)) {
					auxCurrentCards.add(currentCards.get(j));
					currentCards.remove(j);
					j--;
					found = true;

				}

			}
			if (found) {

				auxCurrentCards.add(currentCards.get(i));
				currentCards.remove(i);
				i = 0;
			}

		}

		auxCurrentCards.addAll(currentCards);

		currentCards = auxCurrentCards;

		return 0 == currentCards.get(0).compareTo(currentCards.get(3));
	}

	public int getDiscard() {
		return discard;
	}

	// SELECCIONAMOS QUE CARTA DESCARTAR, SELECCIONANDO CON UN 50% ENTRE LAS 2
	// ÚLTIMAS PARA EVITAR DEADLOCK

	private void seleccionarDescarte() {
		  if (currentCards.get(2) != currentCards.get(0)) {
			if (Math.random() * 100 > 50) {
				if (currentCards.get(2) == currentCards.get(3)) {
					discard = currentCards.get(0);
					currentCards.set(0, currentCards.get(2));
					currentCards.set(1, currentCards.get(2));
					currentCards.set(2, discard);
					currentCards.set(3, discard);

				}
				discard = currentCards.get(3);
			} else {

				discard = currentCards.get(2);
				currentCards.set(2, currentCards.get(3));
				currentCards.set(3, discard);

			}
		} else
			discard = currentCards.get(3);
	}

	private void cambiaCarta() {

		currentCards.set(3, newCard);
	}

}

package com.example.casino.Remik;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class RemikMain {
    @FXML // Player card slots
    ImageView pc1, pc2, pc3, pc4, pc5, pc6, pc7, pc8, pc9, pc10, pc11, pc12, pc13, pc14;

    @FXML // srodkowy stół planszy gdzie dodawane są wyłożone karty
    HBox middleBoard;

    @FXML // Opponent1 card slots
    ImageView o1c1, o1c2, o1c3, o1c4, o1c5, o1c6, o1c7, o1c8, o1c9, o1c10, o1c11, o1c12, o1c13, o1c14;

    @FXML // Opponent2 card slots
    ImageView o2c1, o2c2, o2c3, o2c4, o2c5, o2c6, o2c7, o2c8, o2c9, o2c10, o2c11, o2c12, o2c13, o2c14;

    @FXML // Opponent3 card slots
    ImageView o3c1, o3c2, o3c3, o3c4, o3c5, o3c6, o3c7, o3c8, o3c9, o3c10, o3c11, o3c12, o3c13, o3c14;

    @FXML
    ImageView deckTop;

    @FXML
    Button drawCardButton, takeFromTopButton, tryToLayOffButton, confirmDiscardButton;

    @FXML
    Label cardsLeft, infoDisplay;

    boolean[] pcChosenArray = new boolean[14];

    RemikPlayer player1;
    RemikDeck deck;

    boolean playersTurn = false;
    boolean drawTime = false;

    public void startGameTest() {
        deck = new RemikDeck();
        player1 = new RemikPlayer("player1");
        RemikPlayer player2 = new RemikPlayer("player2");
        RemikPlayer player3 = new RemikPlayer("player3");
        RemikPlayer player4 = new RemikPlayer("player4");

        dealCards(deck, player1);
        dealCards(deck, player2);
        dealCards(deck, player3);
        dealCards(deck, player4);

        displayCards(player1);
        displayCards(player2);
        displayCards(player3);
        displayCards(player4);

        deckTop.setImage(new Image(getClass().getResourceAsStream("/images/cards/back.png")));

        cardsLeft.setText("Cards left in deck: " + deck.cardsLeftInDeck());

        playersTurn = true;
        drawTime = true;

        routine();

        // test kiedy jest tura innego gracza
//        Thread testTurn = new Thread(() -> {
//            try {
//                Thread.sleep(30000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            playersTurn = false;
//            routine();
//        });
//
//        testTurn.start();
    }

    private void displayCards(RemikPlayer player) {
        for (int i = 0; i < player.getCardsOnHand().size(); i++) {
            RemikCard card = player.getCardsOnHand().get(i);
            String imagePath;
            if (player.getName().equals("player1")) {
                imagePath = getImagePath(card);
            } else {
                imagePath = "/images/cards/back.png";
                //imagePath = getImagePath(card);
            }

            ImageView imageView;
            switch (player.getName()) {
                case "player1":
                    imageView = getPlayerImageViewByIndex(i + 1);
                    imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
                    break;
                case "player2":
                    imageView = getOpponent1ImageViewByIndex(i + 1);
                    Image rotatedImage1 = rotateImage(new Image(getClass().getResourceAsStream(imagePath)), 90);
                    imageView.setImage(rotatedImage1);
                    break;
                case "player3":
                    imageView = getOpponent2ImageViewByIndex(i + 1);
                    imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
                    break;
                case "player4":
                    imageView = getOpponent3ImageViewByIndex(i + 1);
                    Image rotatedImage3 = rotateImage(new Image(getClass().getResourceAsStream(imagePath)), 270);
                    imageView.setImage(rotatedImage3);
                    break;
            }
        }
    }

    private void dealCards(RemikDeck deck, RemikPlayer player) {
        for (int i = 0; i < 13; i++) { // początkowo 13 kart dla każdego
            player.addCard(deck.dealOne());
        }
        player.sortCardsBySortValue();
    }

    private String getImagePath(RemikCard card) {
        String imagePath;

        String suit = card.getSuit().toLowerCase();
        suit = suit.substring(0, suit.length() - 1);

        if ((card.getValue() >= 2 && card.getValue() <= 9) || card.getRank().equals("Ten")) {
            imagePath = "/images/cards/" + card.getValue() + suit + ".png";
        } else {
            imagePath = "/images/cards/" + card.getRank().charAt(0) + suit + ".png";
        }

        return imagePath;
    }

    private Image rotateImage(Image image, double angle) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage rotatedImage = new WritableImage(height, width);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedImage.getPixelWriter().setColor(y, width - 1 - x, image.getPixelReader().getColor(x, y));
            }
        }
        return rotatedImage;
    }

    private void toggleCardSelection(int cardIndex, ImageView cardView) {
        boolean isSelected = !pcChosenArray[cardIndex - 1];
        pcChosenArray[cardIndex - 1] = isSelected;
        handlePcClick(cardView, isSelected);
    }

    private void handlePcClick(ImageView pc, boolean chosen) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), pc);
        transition.setToY(chosen ? -20 : 0);
        transition.play();
    }

    private boolean onlyOneCardUp() {
        int count = 0;
        for (boolean b : pcChosenArray) {
            if (b) {
                count++;
                if (count > 1) {
                    return false;
                }
            }
        }
        return count == 1;
    }

    @FXML
    private void drawCard(ActionEvent event) {
        RemikCard drawnCard = deck.dealOne();
        player1.addCard(drawnCard);
        player1.sortCardsBySortValue();
        displayCards(player1);
        cardsLeft.setText("Cards left in deck: " + deck.cardsLeftInDeck());

        drawTime = !drawTime;
        routine();
    }

    @FXML
    private void takeFromTop(ActionEvent event) {
        if (!deck.getDiscardedCards().isEmpty()) {
            player1.addCard(deck.discardedCards.removeLast());
            player1.sortCardsBySortValue();
            displayCards(player1);
            cardsLeft.setText("Cards left in deck: " + deck.cardsLeftInDeck());
            drawTime = !drawTime;
            routine();
        }
    }

    @FXML
    public void tryToLayOff(ActionEvent event) {

        ArrayList<RemikCard> chosenCards = new ArrayList<>();

        // dodaj wszystkie wybrane karty do listy "chosenCards"
        for (int i = 0; i < pcChosenArray.length; i++) {
            if (pcChosenArray[i]) {
                if (i < player1.getCardsOnHand().size()) {
                    chosenCards.add(player1.getCardsOnHand().get(i));
                }
            }
        }

        // sprawdzenie waruków: czy co najmniej 3 karty i jeden z 2 rodzajow sekwensow
        if (chosenCards.size() >= 3 && (isSameSuitWithIncrementingValues(chosenCards) || isDifferentSuitsWithSameValue(chosenCards))) {
            // Jeśli pasuje, usuń wybrane karty
            for (int i = chosenCards.size() - 1; i >= 0; i--) {
                RemikCard card = chosenCards.get(i);
                int cardIndex = player1.getCardsOnHand().indexOf(card);
                if (cardIndex >= 0) {
                    removeCardFromHand(cardIndex + 1, false);
                }
            }

            // podział kart na rzędy
            ArrayList<ArrayList<RemikCard>> rowsOfCards = splitCardsIntoRows(chosenCards);

            // wyświetlanie karty na middleBoard
            for (ArrayList<RemikCard> row : rowsOfCards) {
                displayRowOfCards(row);
            }

            routine();
        }

    }

    private ArrayList<ArrayList<RemikCard>> splitCardsIntoRows(ArrayList<RemikCard> cards) {
        ArrayList<ArrayList<RemikCard>> rows = new ArrayList<>();
        ArrayList<RemikCard> currentRow = new ArrayList<>();

        for (int i = 0; i < cards.size(); i++) {
            currentRow.add(cards.get(i));
            if (i == cards.size() - 1) {
                rows.add(currentRow);
            }
        }

        return rows;
    }

    private void displayRowOfCards(ArrayList<RemikCard> row) {
        // Sortowanie kart w rzędzie po sortValue
        row.sort(Comparator.comparingInt(RemikCard::getSortValue));

        HBox cardRow = new HBox();
        cardRow.setSpacing(-50); // odstęp między kartami

        for (RemikCard card : row) {
            String imagePath = getImagePath(card);
            ImageView temp = new ImageView();
            temp.setFitHeight(150);
            temp.setFitWidth(103);
            temp.setImage(new Image(getClass().getResourceAsStream(imagePath)));

            // Przechowuj odniesienie do karty w ImageView
            temp.setUserData(card);

            // mechanika klikania na kartę na srodku
            temp.setOnMouseClicked(event -> {
                displayCardsInHBox(cardRow);
                tryToAddCardToHBox(cardRow);
            });

            cardRow.getChildren().add(temp);
        }

        addRowToMiddleBoard(cardRow);
    }

    private void addRowToMiddleBoard(HBox cardRow) {
        if (middleBoard.getChildren().isEmpty() || !(middleBoard.getChildren().get(middleBoard.getChildren().size() - 1) instanceof VBox)) {
            VBox vBox = new VBox();
            vBox.setSpacing(10); // odstęp miedzy vboxami
            vBox.setAlignment(Pos.CENTER); // wysrodkowanie
            middleBoard.setSpacing(10); // odstęp miedzy vboxami
            middleBoard.getChildren().add(vBox);
        }

        VBox currentVBox = (VBox) middleBoard.getChildren().get(middleBoard.getChildren().size() - 1);

        if (currentVBox.getChildren().size() == 4) {
            VBox newVBox = new VBox();
            newVBox.setSpacing(10);
            newVBox.setAlignment(Pos.CENTER);
            middleBoard.getChildren().add(newVBox);
            currentVBox = newVBox;
        }

        currentVBox.getChildren().add(cardRow);
    }

    private void displayCardsInHBox(HBox cardRow) {
        for (Node node : cardRow.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                RemikCard card = getCardFromImageView(imageView);
                if (card != null) {
                }
            }
        }
    }

    private RemikCard getCardFromImageView(ImageView imageView) {
        return (RemikCard) imageView.getUserData();
    }

    private void tryToAddCardToHBox(HBox cardRow) {
        if (!drawTime) {
            ArrayList<RemikCard> cardsInRow = new ArrayList<>();
            for (Node node : cardRow.getChildren()) {
                if (node instanceof ImageView) {
                    RemikCard card = getCardFromImageView((ImageView) node);
                    if (card != null) {
                        cardsInRow.add(card);
                    }
                }
            }

            // czy jedna karta wybrana
            int selectedCardIndex = -1;
            for (int i = 0; i < pcChosenArray.length; i++) {
                if (pcChosenArray[i] && i < player1.getCardsOnHand().size()) {
                    selectedCardIndex = i;
                    break;
                }
            }

            if (selectedCardIndex != -1) {
                RemikCard selectedCard = player1.getCardsOnHand().get(selectedCardIndex);

                // sprawdzenie czy po dodaniu warnki bylyby dalej spelnione
                cardsInRow.add(selectedCard);
                if (isSameSuitWithIncrementingValues(cardsInRow) || isDifferentSuitsWithSameValue(cardsInRow)) {
                    cardsInRow.remove(selectedCard); //

                    // usuwa karte z reki
                    removeCardFromHand(selectedCardIndex + 1, false);
                    pcChosenArray[selectedCardIndex] = false;


                    // odaj kartę do middleBoard
                    String imagePath = getImagePath(selectedCard);
                    ImageView temp = new ImageView();
                    temp.setFitHeight(150);
                    temp.setFitWidth(103);
                    temp.setImage(new Image(getClass().getResourceAsStream(imagePath)));

                    temp.setUserData(selectedCard);
                    temp.setOnMouseClicked(event -> {
                        displayCardsInHBox(cardRow);
                        tryToAddCardToHBox(cardRow);
                    });

                    cardRow.getChildren().add(temp);

                    // sortowanie kart w poszczegolnych hboxach
                    sortCardsInHBox(cardRow);

                } else {
                    cardsInRow.remove(selectedCard);
                    System.out.println("Nie można dodać karty: " + selectedCard);
                }
            }
        }
    }

    private void sortCardsInHBox(HBox cardRow) {
        ArrayList<ImageView> cardViews = new ArrayList<>();
        for (Node node : cardRow.getChildren()) {
            if (node instanceof ImageView) {
                cardViews.add((ImageView) node);
            }
        }

        // sortowanie kart po sortValue
        cardViews.sort(Comparator.comparingInt((ImageView imageView) -> ((RemikCard) imageView.getUserData()).getSortValue()));

        // usuwa karty z hbox
        cardRow.getChildren().clear();

        // i dodaje na miejsce usunietych posortowane
        cardRow.getChildren().addAll(cardViews);
    }

    private boolean isSameSuitWithIncrementingValues(ArrayList<RemikCard> cards) {
        cards.sort(Comparator.comparingInt(RemikCard::getSortValue)); // sortowanie kart według "sortValue"
        String suit = cards.get(0).getSuit();
        for (int i = 1; i < cards.size(); i++) {
            if (!cards.get(i).getSuit().equals(suit) || cards.get(i).getSortValue() != cards.get(i - 1).getSortValue() + 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isDifferentSuitsWithSameValue(ArrayList<RemikCard> cards) {
        int value = cards.get(0).getValue();
        HashSet<String> suits = new HashSet<>();
        for (RemikCard card : cards) {
            if (card.getValue() != value || !suits.add(card.getSuit())) {
                return false;
            }
        }
        return true;
    }

    @FXML
    private void confirmDiscard(ActionEvent event) {
        if (onlyOneCardUp()) {
            discardSelectedCard();
            drawTime = !drawTime;
            routine();
        }
    }

    private void routine() {
        // musi być Platform.runLater, bo będzie thread exception, jak zmienia sie coś w javafx thread
        Platform.runLater(() -> {
            if (deck.isDeckEmpty()) {
                deck.refillDeckFromDiescardedCards();
            }

            for (int i = 0; i < pcChosenArray.length; i++) {
                if (i >= player1.getCardsOnHand().size()) {
                    pcChosenArray[i] = false;
                }
            }

            String topCard;
            ImageView topImage = deckTop;
            if (!deck.getDiscardedCards().isEmpty()) {
                topCard = getImagePath(deck.getDiscardedCards().getLast());
            } else {
                topCard = "/images/cards/back.png";
            }
            topImage.setImage(new Image(getClass().getResourceAsStream(topCard)));

            //wyswietlanie nad przyciskami co sie dzieje
            if (playersTurn) {
                if (drawTime) {
                    infoDisplay.setText("Dobierz kartę z talii lub stosu kart odrzuconych:");
                } else {
                    infoDisplay.setText("Odrzuć kartę lub wyłóż karty:");
                }
            } else {
                infoDisplay.setText("Oczekiwanie na zagranie innych graczy...");
            }

            setImageViewsDisableState(player1.getCardsOnHand().size());
            updateButtonsVisibility();
        });
    }

    private void setImageViewsDisableState(int numberOfCards) { // ustawia ilosc aktywnych ImageView dla pc w zależności od liczby kart na ręce
        ImageView[] playerImageViews = {pc1, pc2, pc3, pc4, pc5, pc6, pc7, pc8, pc9, pc10, pc11, pc12, pc13, pc14};

        for (int i = 0; i < playerImageViews.length; i++) {
            if (i < numberOfCards) {
                playerImageViews[i].setDisable(false); // enable ImageView
            } else {
                playerImageViews[i].setDisable(true); // disable ImageView
            }
        }
    }

    private void updateButtonsVisibility() {
        if (playersTurn) {
            if (drawTime) {
                drawCardButton.setDisable(false);
                takeFromTopButton.setDisable(false);
                confirmDiscardButton.setDisable(true);
                tryToLayOffButton.setDisable(true);
            } else {
                drawCardButton.setDisable(true);
                takeFromTopButton.setDisable(true);
                confirmDiscardButton.setDisable(false);
                tryToLayOffButton.setDisable(false);
            }
        } else {

            drawCardButton.setDisable(true);
            takeFromTopButton.setDisable(true);
            confirmDiscardButton.setDisable(true);
            tryToLayOffButton.setDisable(true);
        }

    }

    private void discardSelectedCard() {
        for (int i = 0; i < pcChosenArray.length; i++) {
            if (pcChosenArray[i]) {
                removeCardFromHand(i + 1, true);
                break;
            }
        }

    }

    private void removeCardFromHand(int viewIndex, boolean toDiscardedCards) {
        int cardIndex = viewIndex - 1;
        if (cardIndex >= 0 && cardIndex < player1.getCardsOnHand().size()) {
            ImageView cardView = getPlayerImageViewByIndex(viewIndex);
            resetCardPosition(cardView, () -> {
                if (toDiscardedCards) {
                    deck.discardedCards.add(player1.getCardsOnHand().remove(cardIndex));
                } else {
                    player1.getCardsOnHand().remove(cardIndex);
                }
                cardView.setImage(null);
                cardView.setTranslateY(0);

                pcChosenArray[cardIndex] = false;
                for (int i = cardIndex; i < player1.getCardsOnHand().size(); i++) {
                    pcChosenArray[i] = pcChosenArray[i + 1];
                }
                pcChosenArray[player1.getCardsOnHand().size()] = false;

                for (int i = cardIndex; i < player1.getCardsOnHand().size(); i++) {
                    ImageView currentView = getPlayerImageViewByIndex(i + 1);
                    ImageView nextView = getPlayerImageViewByIndex(i + 2);
                    if (nextView.getImage() != null) {
                        currentView.setImage(nextView.getImage());
                        currentView.setTranslateY(0);
                    } else {
                        currentView.setImage(null);
                    }
                }

                ImageView lastView = getPlayerImageViewByIndex(player1.getCardsOnHand().size() + 1);
                lastView.setImage(null);
                lastView.setTranslateY(0);

                routine();
            });
        } else {
            System.out.println("Index out of bounds: " + viewIndex);
        }
    }

    private void resetCardPosition(ImageView cardView, Runnable onFinished) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), cardView);
        transition.setToY(0);
        transition.setOnFinished(event -> {
            onFinished.run();
            updateButtonsVisibility();
        });
        transition.play();
    }

    private ImageView getPlayerImageViewByIndex(int index) {
        return switch (index) {
            case 1 -> pc1;
            case 2 -> pc2;
            case 3 -> pc3;
            case 4 -> pc4;
            case 5 -> pc5;
            case 6 -> pc6;
            case 7 -> pc7;
            case 8 -> pc8;
            case 9 -> pc9;
            case 10 -> pc10;
            case 11 -> pc11;
            case 12 -> pc12;
            case 13 -> pc13;
            case 14 -> pc14;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    private ImageView getOpponent1ImageViewByIndex(int index) {
        return switch (index) {
            case 1 -> o1c1;
            case 2 -> o1c2;
            case 3 -> o1c3;
            case 4 -> o1c4;
            case 5 -> o1c5;
            case 6 -> o1c6;
            case 7 -> o1c7;
            case 8 -> o1c8;
            case 9 -> o1c9;
            case 10 -> o1c10;
            case 11 -> o1c11;
            case 12 -> o1c12;
            case 13 -> o1c13;
            case 14 -> o1c14;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    private ImageView getOpponent2ImageViewByIndex(int index) {
        return switch (index) {
            case 1 -> o2c1;
            case 2 -> o2c2;
            case 3 -> o2c3;
            case 4 -> o2c4;
            case 5 -> o2c5;
            case 6 -> o2c6;
            case 7 -> o2c7;
            case 8 -> o2c8;
            case 9 -> o2c9;
            case 10 -> o2c10;
            case 11 -> o2c11;
            case 12 -> o2c12;
            case 13 -> o2c13;
            case 14 -> o2c14;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    private ImageView getOpponent3ImageViewByIndex(int index) {
        return switch (index) {
            case 1 -> o3c1;
            case 2 -> o3c2;
            case 3 -> o3c3;
            case 4 -> o3c4;
            case 5 -> o3c5;
            case 6 -> o3c6;
            case 7 -> o3c7;
            case 8 -> o3c8;
            case 9 -> o3c9;
            case 10 -> o3c10;
            case 11 -> o3c11;
            case 12 -> o3c12;
            case 13 -> o3c13;
            case 14 -> o3c14;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    @FXML
    private synchronized void pcClicked(MouseEvent event) {
        Object source = event.getSource();
        if (source instanceof ImageView) {
            ImageView clickedCard = (ImageView) source;
            int cardIndex = getCardIndex(clickedCard);
            if (cardIndex != -1) {
                toggleCardSelection(cardIndex, clickedCard);
            }
        }
    }

    private int getCardIndex(ImageView cardView) {
        if (cardView == pc1) return 1;
        if (cardView == pc2) return 2;
        if (cardView == pc3) return 3;
        if (cardView == pc4) return 4;
        if (cardView == pc5) return 5;
        if (cardView == pc6) return 6;
        if (cardView == pc7) return 7;
        if (cardView == pc8) return 8;
        if (cardView == pc9) return 9;
        if (cardView == pc10) return 10;
        if (cardView == pc11) return 11;
        if (cardView == pc12) return 12;
        if (cardView == pc13) return 13;
        if (cardView == pc14) return 14;
        return -1;
    }
}
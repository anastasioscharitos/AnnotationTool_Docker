package com.example.application.backEnd.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Στο enum LabelYolo απεικονίζονται οι κλάσεις του μοντέλουYolo
 */
public enum LabelsYolo {

    PERSON, BICYCLE,  CAR, MOTORCYCLE, AIRPLANE, BUS, TRAIN,TRUCK,
    BOAT,TRAFFIC_LIGHT, FIRE_HYDRANT,STOP_SIGN, PARKING_METER,
    BENCH, BIRD, CAT, DOG, HORSE, SHEEP, COW, ELEPHANT, BEAR, ZEBRA,
    GIRAFFE, BACKPACK,UMBRELLA, HANDBAG,TIE,SUITCASE,FRISBEE,
    SKIS, SNOWBOARD, SPORTS_BALL, KITE, BASEBALL_BAT, BASEBALL_GLOVE,
    SKATEBOARD, SURFBOARD, TENNIS_RACKET, BOTTLE, WINE_GLASS, CUP,
    FORK, KNIFE, SPOON, BOWL, BANANA, APPLE, SANDWICH, ORANGE,
    BROCCOLI,  CARROT, HOT_DOG, PIZZA,  DONUT, CAKE,CHAIR, COUCH,
    POTTED_PLANT, BED, DINING_TABLE,TOILET, TV, LAPTOP, MOUSE,
    REMOTE, KEYBOARD,  CELL_PHONE, MICROWAVE, OVEN,TOASTER,
    SINK, REFRIGERATOR,BOOK, CLOCK,VASE, SCISSORS, EDDY_BEAR,
    HAIR_DRIER,TOOTHBRUSH;

    LabelsYolo(){}

    /**
     * Η μέθοδος getKeys() επιστρέφει τις κλάσεις του μοντέλου Yolo
     * @return
     */
    public static List<String> getKeys() {
        List<String> list = new ArrayList<>();
        for (LabelsYolo l : LabelsYolo.values()) {
            list.add(l.name());
        }
        return list;
    }
}

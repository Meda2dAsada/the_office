package office.model;

import java.awt.Point;

/**
 * Coordenadas logicas (sobre un lienzo de 920x520) de las estaciones y zonas de la oficina.
 */
public final class OfficeLayout {
    public static final int CANVAS_WIDTH = 920;
    public static final int CANVAS_HEIGHT = 520;

    public static final Point ENTRANCE = new Point(55, 85);
    public static final Point B3_QUEUE = new Point(165, 85);
    public static final Point B1_RECEPTION = new Point(290, 85);
    public static final Point CLIENT_WAIT_AREA = new Point(290, 215);
    public static final Point EXIT = new Point(60, 445);

    public static final Point RECEPTIONIST_DESK = new Point(355, 85);
    public static final Point B4_TRAY = new Point(455, 85);

    public static final Point SELLER_DESK = new Point(575, 115);
    public static final Point B2_PRINTER = new Point(695, 250);
    public static final Point B5_TRAY = new Point(575, 250);

    public static final Point SUPERVISOR_DESK = new Point(455, 345);
    public static final Point B6_TRAY = new Point(575, 395);
    public static final Point ADMIN_DESK = new Point(420, 440);

    public static final Point B7_REST = new Point(790, 395);
    public static final Point BRANCH_GATE = new Point(845, 95);

    private OfficeLayout() {}
}

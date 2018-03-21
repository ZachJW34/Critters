package assignment4;
/* CRITTERS Critter.java
 * EE422C Project 4 submission by
 * Replace <...> with your actual data.
 * <Student1 Name>
 * <Student1 EID>
 * <Student1 5-digit Unique No.>
 * <Student2 Name>
 * <Student2 EID>
 * <Student2 5-digit Unique No.>
 * Slip days used: <0>
 * Fall 2016
 */


import java.util.ArrayList;
import java.util.List;

/* see the PDF for descriptions of the methods and fields in this class
 * you may add fields, methods or inner classes to Critter ONLY if you make your additions private
 * no new public, protected or default-package code or data can be added to Critter
 */


public abstract class Critter {
    private static String myPackage;
    private static List<Critter> population = new java.util.ArrayList<Critter>();
    private static List<Critter> babies = new java.util.ArrayList<Critter>();

    private static List<List<Critter>> world = new ArrayList<>(Params.world_width*Params.world_height);
    private static List<List<Boolean>> hasWalked = new ArrayList<>(Params.world_width*Params.world_height);
    private static Critter[] fighters = new Critter[2];

    // Gets the package name.  This assumes that Critter and its subclasses are all in the same package.
    static {
        for (int i=0; i<Params.world_width*Params.world_height; i++){
            world.add(new ArrayList<Critter>());
            hasWalked.add(new ArrayList<Boolean>());
        }
        myPackage = Critter.class.getPackage().toString().split(" ")[1];
    }

    private static java.util.Random rand = new java.util.Random();

    public static int getRandomInt(int max) {
        return rand.nextInt(max);
    }

    public static void setSeed(long new_seed) {
        rand = new java.util.Random(new_seed);
    }


    /* a one-character long string that visually depicts your critter in the ASCII interface */
    public String toString() {
        return "";
    }

    private int energy = 0;

    protected int getEnergy() {
        return energy;
    }

    private int x_coord;
    private int y_coord;

    protected final void walk(int direction) {
        boolean isFighter = (fighters[0] == this || fighters[1] == this);
        if ( isFighter && checkIfWalked(this)){
            energy-=Params.walk_energy_cost;
            return;
        } else if (isFighter){
            System.out.println("DEBUG");
            int prevX = x_coord;
            int prevY = y_coord;
            move(this, direction, 1);
            if (world.get(convertTo1D(x_coord, y_coord)).isEmpty()){
                x_coord = prevX;
                y_coord = prevY;
            } else{
                System.out.println("DEBUG");
                x_coord = prevX;
                y_coord = prevY;
                energy-=Params.walk_energy_cost;
                return;
            }
        }
        removeFromWorld(this);
        move(this, direction, 1);
        energy-=Params.walk_energy_cost;
        addToWorld(this);
        markAsWalked(this);
    }

    protected final void run(int direction) {
        boolean isFighter = (fighters[0] == this || fighters[1] == this);
        if ( isFighter && checkIfWalked(this)){
            energy-=Params.run_energy_cost;
            return;
        } else if (isFighter){
            int prevX = x_coord;
            int prevY = y_coord;
            move(this, direction, 2);
            if (world.get(convertTo1D(x_coord, y_coord)).isEmpty()){
                x_coord = prevX;
                y_coord = prevY;
            } else{
                x_coord = prevX;
                y_coord = prevY;
                energy-=Params.run_energy_cost;
                return;
            }
        }
        removeFromWorld(this);
        move(this, direction, 2);
        energy-=Params.run_energy_cost;
        addToWorld(this);
        markAsWalked(this);
    }

    private static void move(Critter critter, int direction, int distance){
        if (direction == 7 || direction == 0 || direction == 1){
            critter.x_coord = (critter.x_coord +distance)%Params.world_width;
        }
        if (direction == 3 || direction ==4 || direction == 5){
            critter.x_coord-=distance;
            if (critter.x_coord < 0){
                critter.x_coord+=Params.world_width;
            }
        }
        if (direction == 5 || direction == 6 || direction == 7){
            critter.y_coord = (critter.y_coord + distance)%Params.world_height;
        }
        if (direction == 1 || direction == 2 || direction == 3){
            critter.y_coord-=distance;
            if (critter.y_coord<0){
                critter.y_coord += Params.world_height;
            }
        }

    }

    protected final void reproduce(Critter offspring, int direction) {
        offspring.energy = this.energy/2;
        this.energy = (int) Math.ceil((double)this.energy/2);
        offspring.x_coord = x_coord;
        offspring.y_coord = y_coord;
        move(offspring, direction, 1);
        babies.add(offspring);
    }

    public abstract void doTimeStep();

    public abstract boolean fight(String oponent);

    /**
     * create and initialize a Critter subclass.
     * critter_class_name must be the unqualified name of a concrete subclass of Critter, if not,
     * an InvalidCritterException must be thrown.
     * (Java weirdness: Exception throwing does not work properly if the parameter has lower-case instead of
     * upper. For example, if craig is supplied instead of Craig, an error is thrown instead of
     * an Exception.)
     *
     * @param critter_class_name
     * @throws InvalidCritterException
     */
    public static void makeCritter(String critter_class_name) throws InvalidCritterException {
        if (critter_class_name.length() !=0 && Character.isLowerCase(critter_class_name.charAt(0))){
            throw new InvalidCritterException(critter_class_name);
        }
        try {
            Class critterClass = Class.forName(myPackage + '.' + critter_class_name);
            Critter critter = (Critter) critterClass.newInstance();
            critter.x_coord = getRandomInt(Params.world_width);
            critter.y_coord = getRandomInt(Params.world_height);
            critter.energy = Params.start_energy;
            population.add(critter);
            addToWorld(critter);
            hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoClassDefFoundError e) {
            throw new InvalidCritterException(critter_class_name);
        }
    }

    /**
     * Gets a list of critters of a specific type.
     *
     * @param critter_class_name What kind of Critter is to be listed.  Unqualified class name.
     * @return List of Critters.
     * @throws InvalidCritterException
     */
    public static List<Critter> getInstances(String critter_class_name) throws InvalidCritterException {
        List<Critter> result = new java.util.ArrayList<Critter>();
        try {
            Class critterClass = Class.forName(myPackage + "." + critter_class_name);
            for (Critter critter : population) {
                if (critterClass.equals(critter.getClass())) {
                    result.add(critter);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidCritterException(critter_class_name);
        }
        return result;
    }

    /**
     * Prints out how many Critters of each type there are on the board.
     *
     * @param critters List of Critters.
     */
    public static void runStats(List<Critter> critters) {
        System.out.print("" + critters.size() + " critters as follows -- ");
        java.util.Map<String, Integer> critter_count = new java.util.HashMap<String, Integer>();
        for (Critter crit : critters) {
            String crit_string = crit.toString();
            Integer old_count = critter_count.get(crit_string);
            if (old_count == null) {
                critter_count.put(crit_string, 1);
            } else {
                critter_count.put(crit_string, old_count.intValue() + 1);
            }
        }
        String prefix = "";
        for (String s : critter_count.keySet()) {
            System.out.print(prefix + s + ":" + critter_count.get(s));
            prefix = ", ";
        }
        System.out.println();
    }

    /* the TestCritter class allows some critters to "cheat". If you want to
     * create tests of your Critter model, you can create subclasses of this class
     * and then use the setter functions contained here.
     *
     * NOTE: you must make sure that the setter functions work with your implementation
     * of Critter. That means, if you're recording the positions of your critters
     * using some sort of external grid or some other data structure in addition
     * to the x_coord and y_coord functions, then you MUST update these setter functions
     * so that they correctly update your grid/data structure.
     */
    static abstract class TestCritter extends Critter {
        protected void setEnergy(int new_energy_value) {
            super.energy = new_energy_value;
        }

        protected void setX_coord(int new_x_coord) {
            Critter.removeFromWorld(this);
            super.x_coord = new_x_coord;
            Critter.addToWorld(this);
        }

        protected void setY_coord(int new_y_coord) {
            Critter.removeFromWorld(this);
            super.y_coord = new_y_coord;
            Critter.addToWorld(this);
        }

        protected int getX_coord() {
            return super.x_coord;
        }

        protected int getY_coord() {
            return super.y_coord;
        }


        /*
         * This method getPopulation has to be modified by you if you are not using the population
         * ArrayList that has been provided in the starter code.  In any case, it has to be
         * implemented for grading tests to work.
         */
        protected static List<Critter> getPopulation() {
            return population;
        }

        /*
         * This method getBabies has to be modified by you if you are not using the babies
         * ArrayList that has been provided in the starter code.  In any case, it has to be
         * implemented for grading tests to work.  Babies should be added to the general population
         * at either the beginning OR the end of every timestep.
         */
        protected static List<Critter> getBabies() {
            return babies;
        }
    }

    /**
     * Clear the world of all critters, dead and alive
     */
    public static void clearWorld() {
        population.clear();
        for (int i=0; i<world.size(); i++){
            world.get(i).clear();
            hasWalked.get(i).clear();
        }
    }

    public static void worldTimeStep() {
        for (Critter critter : population) {
            critter.doTimeStep();
        }

        removeDead();

        for (List<Critter> critterList: world){
            while (critterList.size() >=2){
                fighters[0] = critterList.get(0);
                fighters[1] = critterList.get(1);
                Critter loser = battle(fighters[0], fighters[1]);
                if (loser != null) {
                    removeFromWorld(loser);
                    population.remove(loser);
                }
                fighters[0] = null;
                fighters[1] = null;
            }
        }

        for (Critter critter: babies){
            addToWorld(critter);
            population.add(critter);
        }
        babies.clear();

        for (int i=0; i<Params.refresh_algae_count; i++){
            try {
                makeCritter(Algae.class.getSimpleName());
            } catch (InvalidCritterException e){
                e.printStackTrace();
            }
        }

        for (Critter critter: population){
            critter.energy-=Params.rest_energy_cost;
            int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
            hasWalked.get(convertTo1D(critter.x_coord,critter.y_coord)).set(index, false);
        }

        removeDead();

    }

    private static Critter battle(Critter A, Critter B){
        boolean action1 = A.fight(B.toString());
        boolean action2 = B.fight(A.toString());
        boolean didOneDie = removeDead();
        if ((A.x_coord != B.x_coord) || (A.y_coord !=B.y_coord) || didOneDie){
            return null;
        }

        int rollA = action1 ? getRandomInt(A.energy) : 0;
        int rollB = action2 ? getRandomInt(B.energy) : 0;

        if (rollA > rollB){
            A.energy+=B.energy/2;
            return B;
        } else if (rollA < rollB){
            B.energy+=A.energy/2;
            return A;
        } else{
            int random = getRandomInt(2);
            if (random == 0){
                A.energy += B.energy/2;
                return B;
            } else{
                B.energy += A.energy/2;
                return A;
            }
        }
    }

    private static boolean removeDead(){
        boolean didOneDie = false;
        List<Critter> toRemove = new ArrayList<>();
        for (Critter critter: population){
            if (critter.energy <= 0) {
                didOneDie = true;
                removeFromWorld(critter);
                toRemove.add(critter);
            }
        }
        for (Critter critter: toRemove){
            population.remove(critter);
        }
        return didOneDie;
    }

    public static void displayWorld() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('+');
        for (int i=0; i<Params.world_width; i++){
            stringBuilder.append('-');
        }
        stringBuilder.append("+\n");
        for (int y=0; y<Params.world_height; y++){
            stringBuilder.append('|');
            for (int x=0; x<Params.world_width; x++){
                List<Critter> temp = world.get(convertTo1D(x,y));
                if (temp.isEmpty()){
                    stringBuilder.append(" ");
                } else {
                    stringBuilder.append(temp.get(0).toString());
                }
            }
            stringBuilder.append("|\n");
        }
        stringBuilder.append('+');
        for (int i=0; i<Params.world_width; i++){
            stringBuilder.append('-');
        }
        stringBuilder.append("+");
        System.out.println(stringBuilder.toString());
    }


    private static int convertTo1D(int x, int y){
        int result = y*Params.world_width+x;
        return result;
    }

    private static void addToWorld(Critter critter){
        world.get(convertTo1D(critter.x_coord, critter.y_coord)).add(critter);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).add(false);
    }

    private static void removeFromWorld(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        world.get(convertTo1D(critter.x_coord, critter.y_coord)).remove(index);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).remove(index);
    }

    private static void markAsWalked(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).set(index, true);
    }

    private static boolean checkIfWalked(Critter critter){
        int index = world.get(convertTo1D(critter.x_coord, critter.y_coord)).indexOf(critter);
        boolean result = hasWalked.get(convertTo1D(critter.x_coord, critter.y_coord)).get(index);
        if (result == true){
            System.out.println("DEBUG");
        }
        return result;
    }


}





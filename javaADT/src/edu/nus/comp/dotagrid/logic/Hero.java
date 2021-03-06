package edu.nus.comp.dotagrid.logic;

import java.util.ArrayList;


public class Hero extends Character{
	
	public static ArrayList<Pair<Hero, Integer>> reviveQueue = new ArrayList<Pair<Hero, Integer>>();
	
	private String mainAttribute;
	private double basicMainAttribute;
	private double totalMainAttribute;
	
	private int heroSpawningXPos = -1;
	private int heroSpawningYPos = -1;
	
	
	// when display in the game frame, the format will be <basic attribute> + " + " + <attributes obtained from items>
	
	// starting attribute is given when constructing the hero
	private double startingStrength, startingAgility, startingIntelligence;
	private double strengthGrowth, agilityGrowth, intelligenceGrowth;
	
	// basic attribute is calculated by taking  (startingAttributes + growth * level + attributes obtained from skills)
	private double basicStrength, basicAgility, basicIntelligence;
	
	// total attribute is calculated by taking basic attribute + attributes obtained from items
	private double totalStrength, totalAgility, totalIntelligence;

	private int level;
	private int experience;

	// skills and total attributes added from skills
	public Skill[] skills;
	private int unusedSkillCount = 1;
	private double totalSkillAddStrength, totalSkillAddAgility, totalSkillAddIntelligence;
	private int totalSkillAddHP, totalSkillAddMP;
	private double totalSkillAddHPGainPerRound, totalSkillAddMPGainPerRound;
	private double totalSkillAddPhysicalDefence, totalSkillAddMagicResistance;
	private double totalSkillAddPhysicalAttack, totalSkillAddPhysicalAttackSpeed;
	private int totalSkillAddPhysicalAttackArea;
	private int totalSkillAddMovementSpeed;
	
	// items and total attributes added from items
	public Item[] items;
	private double totalItemAddStrength, totalItemAddAgility, totalItemAddIntelligence;
	private int totalItemAddHP, totalItemAddMP;
	private double totalItemAddHPGainPerRound, totalItemAddMPGainPerRound;
	private double totalItemAddPhysicalDefence, totalItemAddMagicResistance;
	private double totalItemAddPhysicalAttack, totalItemAddPhysicalAttackSpeed;
	private int totalItemAddPhysicalAttackArea;
	private int totalItemAddMovementSpeed;
	

	private int kill, death, assist;
	private int money;
	
	// define a list of constants for calculation
	public static final int STRENGTH_ADD_HP_RATIO = 19;
	public static final int INTELLIGENCE_ADD_MP_RATIO = 13;
	
	public static final double STRENGTH_ADD_HP_PER_ROUND = 0.3;
	public static final double INTELLIGENCE_ADD_MP_PER_ROUND = 0.4;
	public static final double AGILITY_ADD_PHYSICAL_DEFENCE_RATIO = 0.02;
	
	public static final double AGILITY_ADD_PHYSICAL_ATTACK_SPEED_RATIO = 0.01;
	public static final double MAIN_ATTRIBUTE_ADD_PHYSICAL_ATTACK_RATIO = 1.0;
	

	public static double HeroHPGainPerRound = 1.0;
	public static double HeroMPGainPerRound = 2.5;

	
	// constants used in hero constructor
	public static int heroStartingBountyMoney = 250;
	public static int heroStartingBountyExp = 100;
	public static int heroStartingMoney = 875;
	public static int heroSight = 7;
	
	public static final int HERO_ATTACK_PRIORITY = 5;
	public static final int HERO_PER_ROUND_MONEY_INCREASE = 10;
	
	
	// constructor
	public Hero(String heroName, String mainAttribute, int startingHP, int startingMP, 
			double startingPhysicalAttack, int startingPhysicalAttackArea, double startingPhysicalAttackSpeed, 
			double startingPhysicalDefence, double startingMagicResistance, int actionPoint, int teamNumber,
			int startingStrength, int startingAgility, int startingIntelligence, 
			double strengthGrowth, double agilityGrowth, double intelligenceGrowth, int movementSpeed) 
	{
		
		super(heroName, heroStartingBountyMoney, heroStartingBountyExp, heroSight, 
				startingHP, startingMP, startingPhysicalAttack, startingPhysicalAttackArea, startingPhysicalAttackSpeed, 
				startingPhysicalDefence, startingMagicResistance, movementSpeed, actionPoint, HERO_ATTACK_PRIORITY, teamNumber);
		
		// reset basic physical attack
		this.setBasicPhysicalAttack(this.getStartingPhysicalAttack());
		
		// initialize attributes specific to heros
		this.setMainAttribute(mainAttribute);
	
		this.setLevel(1);
		this.setExperience(0);
		
		this.setMoney(heroStartingMoney);
		this.setKill(0);
		this.setAssist(0);
		this.setDeath(0);

		this.setStrengthGrowth(strengthGrowth);
		this.setAgilityGrowth(agilityGrowth);
		this.setIntelligenceGrowth(intelligenceGrowth);	
		
		this.setStartingStrength(startingStrength);
		this.setStartingAgility(startingAgility);
		this.setStartingIntelligence(startingIntelligence);

		this.setStartingHP(startingHP + startingStrength * STRENGTH_ADD_HP_RATIO);
		this.setStartingMP(startingMP + startingIntelligence * INTELLIGENCE_ADD_MP_RATIO);
		
		this.setBasicStrength(this.getStartingStrength());
		this.setBasicAgility(this.getStartingAgility());
		this.setBasicIntelligence(this.getStartingIntelligence());
		
		this.setTotalStrength(this.getStartingStrength());
		this.setTotalAgility(this.getStartingAgility());
		this.setTotalIntelligence(this.getStartingIntelligence());
		
		this.setBasicMainAttribute();
		this.setTotalMainAttribute();
		
		this.setmaxHP((int) (this.getStartingHP() + this.getTotalStrength() * STRENGTH_ADD_HP_RATIO));
		this.setmaxMP((int) (this.getStartingMP() + this.getTotalIntelligence() * INTELLIGENCE_ADD_MP_RATIO));
		this.setCurrentHP(this.getmaxHP());
		this.setCurrentMP(this.getmaxMP());
		
		this.setHPGainPerRound(this.getTotalStrength() * STRENGTH_ADD_HP_PER_ROUND + HeroHPGainPerRound);
		this.setMPGainPerRound(this.getTotalIntelligence() * INTELLIGENCE_ADD_MP_PER_ROUND + HeroMPGainPerRound);

		this.setTotalPhysicalDefence(this.getBasicPhysicalDefence() + this.getTotalAgility() * AGILITY_ADD_PHYSICAL_DEFENCE_RATIO);		
		this.setTotalPhysicalAttackSpeed(this.getStartingPhysicalAttackSpeed() + this.getTotalAgility() * AGILITY_ADD_PHYSICAL_ATTACK_SPEED_RATIO);
		
		this.setTotalPhysicalAttack(this.getBasicPhysicalAttack() + this.getTotalMainAttribute() * MAIN_ATTRIBUTE_ADD_PHYSICAL_ATTACK_RATIO);
		
		this.items = new Item[GameFrame.MAX_ITEM_NUMBER];
		this.skills = new Skill[GameFrame.MAX_SKILL_NUMBER];
	}
	


	public Hero(Hero hero) {
		super(hero.getName(), hero.getBountyMoney(), hero.getBountyExp(), hero.getSight(), hero.getStartingHP(), hero.getStartingMP(), 
				hero.getStartingPhysicalAttack(), hero.getStartingPhysicalAttackArea(), hero.getStartingPhysicalAttackSpeed(), 
				hero.getStartingPhysicalDefence(), hero.getStartingMagicResistance(), 
				hero.getStartingMovementSpeed(), hero.getMaxActionPoint(), HERO_ATTACK_PRIORITY, hero.getTeamNumber());

		this.setCurrentAttackPriority(hero.getCurrentAttackPriority());
		
		this.setHeroSpawningXPos(hero.getHeroSpawningXPos());
		this.setHeroSpawningYPos(hero.getHeroSpawningYPos());
		
		this.setCurrentActionPoint(hero.getCurrentActionPoint());
		
		this.setMainAttribute(hero.getMainAttribute());
		
		this.setLevel(hero.getLevel());
		this.setExperience(hero.getExperience());
		
		this.setUnusedSkillCount(hero.getUnusedSkillCount());
		
		this.setBountyExp(CalculateLevelInfo.calculateBountyExp(this.getLevel()));
		
		this.setMoney(hero.getMoney());
		this.setKill(hero.getKill());
		this.setAssist(hero.getAssist());
		this.setDeath(hero.getDeath());

		this.setStrengthGrowth(hero.getStrengthGrowth());
		this.setAgilityGrowth(hero.getAgilityGrowth());
		this.setIntelligenceGrowth(hero.getIntelligenceGrowth());	
			
		this.setStartingStrength(hero.getStartingStrength());
		this.setStartingAgility(hero.getStartingAgility());
		this.setStartingIntelligence(hero.getStartingIntelligence());
	
		this.setStartingHP(hero.getStartingHP());
		this.setStartingMP(hero.getStartingMP());
		
		this.setBasicStrength(hero.getBasicStrength());
		this.setBasicAgility(hero.getBasicAgility());
		this.setBasicIntelligence(hero.getBasicIntelligence());
		
		this.setTotalStrength(hero.getTotalStrength());
		this.setTotalAgility(hero.getTotalAgility());
		this.setTotalIntelligence(hero.getTotalIntelligence());
		
		this.setBasicMainAttribute();
		this.setTotalMainAttribute();
		
		this.setmaxHP(hero.getmaxHP());
		this.setmaxMP(hero.getmaxMP());
		this.setCurrentHP(hero.getCurrentHP());
		this.setCurrentMP(hero.getCurrentMP());
		
		this.setHPGainPerRound(hero.getHPGainPerRound());
		this.setMPGainPerRound(hero.getMPGainPerRound());
		
		this.setBasicPhysicalDefence(hero.getBasicPhysicalDefence());
		this.setTotalPhysicalDefence(hero.getTotalPhysicalDefence());		
		this.setTotalPhysicalAttackSpeed(hero.getTotalPhysicalAttackSpeed());
		
		this.setBasicPhysicalAttack(hero.getBasicPhysicalAttack());
		this.setTotalPhysicalAttack(hero.getTotalPhysicalAttack());
		
		this.items = new Item[GameFrame.MAX_ITEM_NUMBER];
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++){
			if (hero.items[i] != null){
				this.items[i] = new Item(hero.items[i]);
			}
		}
		
		
		this.skills = new Skill[GameFrame.MAX_SKILL_NUMBER];
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++){
			if (hero.skills[i] != null){
				this.skills[i] = new Skill(hero.skills[i]);
			}
		}
		
	}



	public void updateHeroAttributeInfo(){
		this.setBasicStrength(this.getStartingStrength() + (this.getLevel() - 1) * this.getStrengthGrowth());
		this.setBasicAgility(this.getStartingAgility() + (this.getLevel() - 1) * this.getAgilityGrowth());
		this.setBasicIntelligence(this.getStartingIntelligence() + (this.getLevel() - 1) * this.getIntelligenceGrowth());
		
		this.setTotalStrength(this.getBasicStrength() + this.getTotalItemAddStrength() + this.getTotalSkillAddStrength());
		this.setTotalAgility(this.getBasicAgility() + this.getTotalItemAddAgility() + this.getTotalSkillAddAgility());
		this.setTotalIntelligence(this.getBasicIntelligence() + this.getTotalItemAddIntelligence() + this.getTotalSkillAddIntelligence());
		
		this.setBasicMainAttribute();
		this.setTotalMainAttribute();

		this.setmaxHP((int) (this.getStartingHP() + this.getTotalStrength() * STRENGTH_ADD_HP_RATIO 
				+ this.getTotalItemAddHP() + this.getTotalSkillAddHP()));
		this.setmaxMP((int) (this.getStartingMP() + this.getTotalIntelligence() * INTELLIGENCE_ADD_MP_RATIO 
				+ this.getTotalItemAddMP() + this.getTotalSkillAddMP()));

		this.setHPGainPerRound(HeroHPGainPerRound + this.getTotalStrength() * STRENGTH_ADD_HP_PER_ROUND 
				+ this.getTotalItemAddHPGainPerRound() + this.getTotalSkillAddHPGainPerRound());
		this.setMPGainPerRound(HeroMPGainPerRound + this.getTotalIntelligence() * INTELLIGENCE_ADD_MP_PER_ROUND 
				+ this.getTotalItemAddMPGainPerRound() +  + this.getTotalSkillAddMPGainPerRound());
		
		this.setTotalPhysicalAttackSpeed(this.getStartingPhysicalAttackSpeed() + this.getTotalAgility() * AGILITY_ADD_PHYSICAL_ATTACK_SPEED_RATIO
				+ this.getTotalItemAddPhysicalAttackSpeed() + this.getTotalSkillAddPhysicalAttackSpeed());
		this.setTotalPhysicalAttackArea(this.getStartingPhysicalAttackArea() 
				+ this.getTotalItemAddPhysicalAttackArea() + this.getTotalSkillAddPhysicalAttackArea());
		this.setTotalMagicResistance(this.getStartingMagicResistance() 
				+ this.getTotalItemAddMagicResistance() + this.getTotalSkillAddMagicResistance());
		this.setTotalMovementSpeed(this.getStartingMovementSpeed() 
				+ this.getTotalItemAddMovementSpeed() + this.getTotalSkillAddMovementSpeed());
		
		this.setBasicPhysicalDefence(this.getStartingPhysicalDefence() + this.getBasicAgility() * AGILITY_ADD_PHYSICAL_DEFENCE_RATIO);	
		this.setTotalPhysicalDefence(this.getBasicPhysicalDefence() + (this.getTotalAgility() - this.getBasicAgility()) * AGILITY_ADD_PHYSICAL_DEFENCE_RATIO
				+ this.getTotalItemAddPhysicalDefence() + this.getTotalSkillAddPhysicalDefence());
		
		this.setBasicPhysicalAttack(this.getStartingPhysicalAttack() + this.getBasicMainAttribute() * MAIN_ATTRIBUTE_ADD_PHYSICAL_ATTACK_RATIO);
		this.setTotalPhysicalAttack(this.getStartingPhysicalAttack() + this.getTotalMainAttribute() * MAIN_ATTRIBUTE_ADD_PHYSICAL_ATTACK_RATIO
				+ this.getTotalItemAddPhysicalAttack() + this.getTotalSkillAddPhysicalAttack());
	}
	

	// accessor and mutator for hero spawning position
	
	public int getHeroSpawningXPos() {
		return heroSpawningXPos;
	}

	public void setHeroSpawningXPos(int heroSpawningXPos) {
		this.heroSpawningXPos = heroSpawningXPos;
	}

	public int getHeroSpawningYPos() {
		return heroSpawningYPos;
	}

	public void setHeroSpawningYPos(int heroSpawningYPos) {
		this.heroSpawningYPos = heroSpawningYPos;
	}


	
	// accessor and mutator for three primary attributes: strength, agility, intelligence

	public String getMainAttribute() {
		return mainAttribute;
	}

	public void setMainAttribute(String mainAttribute) {
		if (mainAttribute.equalsIgnoreCase("strength") || 
			mainAttribute.equalsIgnoreCase("agility") || 
			mainAttribute.equalsIgnoreCase("intelligence")) {
			
			this.mainAttribute = mainAttribute.toLowerCase();
		} else {
			System.out.println("Invalid Main Attribute Type!");
		}		
	}
	
	public double getBasicMainAttribute() {
		return basicMainAttribute;
	}


	public void setBasicMainAttribute() {
		if (this.getMainAttribute().equalsIgnoreCase("strength")) {
			this.basicMainAttribute = this.getBasicStrength();
			
		} else if (this.getMainAttribute().equalsIgnoreCase("agility")){
			this.basicMainAttribute = this.getBasicAgility();
			
		} else if (this.getMainAttribute().equalsIgnoreCase("intelligence")){
			this.basicMainAttribute = this.getBasicIntelligence();
			
		} else {
			System.out.println("Error occurs when trying to set total main attribute!");
		}
	}
	
	public double getTotalMainAttribute() {
		return totalMainAttribute;
	}


	public void setTotalMainAttribute() {
		if (this.getMainAttribute().equalsIgnoreCase("strength")) {
			this.totalMainAttribute = this.getTotalStrength();
			
		} else if (this.getMainAttribute().equalsIgnoreCase("agility")){
			this.totalMainAttribute = this.getTotalAgility();
			
		} else if (this.getMainAttribute().equalsIgnoreCase("intelligence")){
			this.totalMainAttribute = this.getTotalIntelligence();
			
		} else {
			System.out.println("Error occurs when trying to set total main attribute!");
		}
	}

	
	public double getStartingStrength() {
		return startingStrength;
	}


	public void setStartingStrength(double startingStrength) {
		// minimum value is 0
		if (startingStrength <= 0) {
			System.out.println("startingStrength must be positive!");
		} else {
			this.startingStrength = startingStrength;
		}
	}


	public double getStartingAgility() {
		return startingAgility;
	}


	public void setStartingAgility(double startingAgility) {
		// minimum value is 0
		if (startingAgility <= 0) {
			System.out.println("startingAgility must be positive!");
		} else {
			this.startingAgility = startingAgility;
		}
	}


	public double getStartingIntelligence() {
		return startingIntelligence;
	}


	public void setStartingIntelligence(double startingIntelligence) {
		// minimum value is 0
		if (startingIntelligence <= 0) {
			System.out.println("startingIntelligence must be positive!");
		} else {
			this.startingIntelligence = startingIntelligence;
		}
	}


	public double getStrengthGrowth() {
		return strengthGrowth;
	}


	public void setStrengthGrowth(double strengthGrowth) {
		// minimum value is 0
		if (strengthGrowth <= 0) {
			System.out.println("strengthGrowth must be positive!");
		} else {
			this.strengthGrowth = strengthGrowth;
		}
	}


	public double getAgilityGrowth() {
		return agilityGrowth;
	}


	public void setAgilityGrowth(double agilityGrowth) {
		// minimum value is 0
		if (agilityGrowth <= 0) {
			System.out.println("agilityGrowth must be positive!");
		} else {
			this.agilityGrowth = agilityGrowth;
		}
	}


	public double getIntelligenceGrowth() {
		return intelligenceGrowth;
	}


	public void setIntelligenceGrowth(double intelligenceGrowth) {
		// minimum value is 0
		if (intelligenceGrowth <= 0) {
			System.out.println("intelligenceGrowth must be positive!");
		} else {
			this.intelligenceGrowth = intelligenceGrowth;
		}
	}

	public double getBasicStrength() {
		return basicStrength;
	}


	public void setBasicStrength(double basicStrength) {
		this.basicStrength = basicStrength;
	}


	public double getBasicAgility() {
		return basicAgility;
	}


	public void setBasicAgility(double basicAgility) {
		this.basicAgility = basicAgility;
	}


	public double getBasicIntelligence() {
		return basicIntelligence;
	}


	public void setBasicIntelligence(double basicIntelligence) {
		this.basicIntelligence = basicIntelligence;
	}


	public double getTotalStrength() {
		return totalStrength;
	}


	public void setTotalStrength(double totalStrength) {
		// minimum value is startingStrength
		if (totalStrength <= startingStrength){
			this.totalStrength = startingStrength;
		} else {
			this.totalStrength = totalStrength;
		}
	}


	public double getTotalAgility() {
		return totalAgility;
	}


	public void setTotalAgility(double totalAgility) {
		// minimum value is startingAgility
		if (totalAgility <= startingAgility){
			this.totalAgility = startingAgility;
		} else {
			this.totalAgility = totalAgility;
		}
	}


	public double getTotalIntelligence() {
		return totalIntelligence;
	}


	public void setTotalIntelligence(double totalIntelligence) {
		// minimum value is startingIntelligence
		if (totalIntelligence <= startingIntelligence){
			this.totalIntelligence = startingIntelligence;
		} else {
			this.totalIntelligence = totalIntelligence;
		}
	}

	
	// accessor and mutator for level and experience

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;

		// change hero's level based on experience gained
		CalculateLevelInfo newLevel = new CalculateLevelInfo(experience);
		
		// if hero levels up, add one unused skill count
		this.setUnusedSkillCount(this.getUnusedSkillCount() + (newLevel.getLevel() - this.getLevel()));
		
		this.setLevel(newLevel.getLevel());
	}


	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}



	// KDA & Money
	
	public int getKill() {
		return kill;
	}

	public void setKill(int kill) {
		this.kill = kill;
	}

	public int getDeath() {
		return death;
	}

	public void setDeath(int death) {
		this.death = death;
	}

	public int getAssist() {
		return assist;
	}

	public void setAssist(int assist) {
		this.assist = assist;
	}

	
	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		if (money <= 0) {
			this.money = 0;
		} else {
			this.money = money;
		}
	}
	
	
	// attributes obtained from items
	
	public double getTotalItemAddStrength() {
		this.totalItemAddStrength = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddStrength += items[i].getAddStrength();
		}
		return this.totalItemAddStrength;
	}


	public double getTotalItemAddAgility() {
		this.totalItemAddAgility = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddAgility += items[i].getAddAgility();
		}
		return this.totalItemAddAgility;
	}


	public double getTotalItemAddIntelligence() {
		this.totalItemAddIntelligence = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddIntelligence += items[i].getAddIntelligence();
		}
		return this.totalItemAddIntelligence;
	}


	public int getTotalItemAddHP() {
		this.totalItemAddHP = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddHP += items[i].getAddHP();
			}
		}
		return this.totalItemAddHP;
	}

	public int getTotalItemAddMP() {
		this.totalItemAddMP = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddMP += items[i].getAddMP();
			}
		}
		return this.totalItemAddMP;
	}
	

	public double getTotalItemAddHPGainPerRound() {
		this.totalItemAddHPGainPerRound = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddHPGainPerRound += items[i].getAddHPGainPerRound();
			}
		}
		return this.totalItemAddHPGainPerRound;
	}

	
	public double getTotalItemAddMPGainPerRound() {
		this.totalItemAddMPGainPerRound = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddMPGainPerRound += items[i].getAddMPGainPerRound();
			}
		}
		return this.totalItemAddMPGainPerRound;
	}

	
	public double getTotalItemAddPhysicalDefence() {
		this.totalItemAddPhysicalDefence = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddPhysicalDefence += items[i].getAddPhysicalDefence();
			}
		}
		return this.totalItemAddPhysicalDefence;
	}

	
	public double getTotalItemAddMagicResistance() {
		this.totalItemAddMagicResistance = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddMagicResistance += items[i].getAddMagicResistance();
		}
		return this.totalItemAddMagicResistance;
	}


	public double getTotalItemAddPhysicalAttack() {
		this.totalItemAddPhysicalAttack = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddPhysicalAttack += items[i].getAddPhysicalAttack();
			}
		}
		return this.totalItemAddPhysicalAttack;
	}

	public double getTotalItemAddPhysicalAttackSpeed() {
		this.totalItemAddPhysicalAttackSpeed = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null) {
				this.totalItemAddPhysicalAttackSpeed += items[i].getAddPhysicalAttackSpeed();
			}
		}
		return this.totalItemAddPhysicalAttackSpeed;
	}
	

	public int getTotalItemAddPhysicalAttackArea() {
		this.totalItemAddPhysicalAttackArea = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddPhysicalAttackArea += items[i].getAddPhysicalAttackArea();
		}
		return this.totalItemAddPhysicalAttackArea;
	}
	

	public int getTotalItemAddMovementSpeed() {
		this.totalItemAddMovementSpeed = 0;
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++) {
			if (this.items[i] != null)
				this.totalItemAddMovementSpeed += items[i].getAddMovementSpeed();
		}
		return this.totalItemAddMovementSpeed;
	}
	
	
	// add in an item
	public void addItem(Item item){
		for (int i=0; i<GameFrame.MAX_ITEM_NUMBER; i++){
			if(this.items[i] == null){
				this.items[i] = new Item(item);
				GameFrame.allCharacterInfoGameButtons.get(11 + i).setImage(item.getItemImage().getImage());
				System.out.println("a new item has been added to player's inventory list!");
				break;
			}
		}
	}
	
	// sell an item
	public void removeItem(int itemNumber){
		// add the selling price to player's money
		((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).setMoney(
				(((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).getMoney() 
						+ ((Hero)GridFrame.gridButtonMap[Screen.user.player.getXPos()][Screen.user.player.getYPos()].getCharacter()).items[itemNumber].getSellPrice())); 
		
		// delete the item
		this.items[itemNumber] = null;
		GameFrame.allCharacterInfoGameButtons.get(11 + itemNumber).setIsReadyToDrawImage(false);
		GameFrame.allCharacterInfoGameButtons.get(11 + itemNumber).setImage("");
	}
	
	
	// attributes contributed from skill

	public void setTotalSkillAddStrength(double totalSkillAddStrength) {
		this.totalSkillAddStrength = totalSkillAddStrength;
	}



	public void setTotalSkillAddAgility(double totalSkillAddAgility) {
		this.totalSkillAddAgility = totalSkillAddAgility;
	}



	public void setTotalSkillAddIntelligence(double totalSkillAddIntelligence) {
		this.totalSkillAddIntelligence = totalSkillAddIntelligence;
	}



	public double getTotalSkillAddStrength() {
		this.totalSkillAddStrength = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null) {
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddStrength += skills[i].getAddStrength();
			}
		}
		return this.totalSkillAddStrength;
	}


	public double getTotalSkillAddAgility() {
		this.totalSkillAddAgility = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddAgility += skills[i].getAddAgility();
		}
		return this.totalSkillAddAgility;
	}


	public double getTotalSkillAddIntelligence() {
		this.totalSkillAddIntelligence = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddIntelligence += skills[i].getAddIntelligence();
		}
		return this.totalSkillAddIntelligence;
	}

	
	
	public int getTotalSkillAddHP() {
		this.totalSkillAddHP = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0) {
					this.totalSkillAddHP += skills[i].getAddHP();
				}
		}
		return this.totalSkillAddHP;
	}



	public void setTotalSkillAddHP(int totalSkillAddHP) {
		this.totalSkillAddHP = totalSkillAddHP;
	}



	public int getTotalSkillAddMP() {
		this.totalSkillAddMP = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddMP += skills[i].getAddMP();
		}
		return this.totalSkillAddMP;
	}



	public void setTotalSkillAddMP(int totalSkillAddMP) {
		this.totalSkillAddMP = totalSkillAddMP;
	}



	public double getTotalSkillAddHPGainPerRound() {
		this.totalSkillAddHPGainPerRound = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddHPGainPerRound += skills[i].getAddHPGainPerRound();
		}
		return this.totalSkillAddHPGainPerRound;
	}



	public void setTotalSkillAddHPGainPerRound(double totalSkillAddHPGainPerRound) {
		this.totalSkillAddHPGainPerRound = totalSkillAddHPGainPerRound;
	}



	public double getTotalSkillAddMPGainPerRound() {
		this.totalSkillAddMPGainPerRound = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddMPGainPerRound += skills[i].getAddMPGainPerRound();
		}
		return this.totalSkillAddMPGainPerRound;
	}



	public void setTotalSkillAddMPGainPerRound(double totalSkillAddMPGainPerRound) {
		this.totalSkillAddMPGainPerRound = totalSkillAddMPGainPerRound;
	}



	public double getTotalSkillAddPhysicalDefence() {
		this.totalSkillAddPhysicalDefence = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddPhysicalDefence += skills[i].getAddPhysicalDefence();
		}
		return this.totalSkillAddPhysicalDefence;
	}



	public void setTotalSkillAddPhysicalDefence(double totalSkillAddPhysicalDefence) {
		this.totalSkillAddPhysicalDefence = totalSkillAddPhysicalDefence;
	}



	public double getTotalSkillAddMagicResistance() {
		this.totalSkillAddMagicResistance = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddMagicResistance += skills[i].getAddMagicResistance();
		}
		return this.totalSkillAddMagicResistance;
	}



	public void setTotalSkillAddMagicResistance(double totalSkillAddMagicResistance) {
		this.totalSkillAddMagicResistance = totalSkillAddMagicResistance;
	}



	public double getTotalSkillAddPhysicalAttack() {
		this.totalSkillAddPhysicalAttack = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddPhysicalAttack += skills[i].getAddPhysicalAttack();
		}
		return this.totalSkillAddPhysicalAttack;
	}



	public void setTotalSkillAddPhysicalAttack(double totalSkillAddPhysicalAttack) {
		this.totalSkillAddPhysicalAttack = totalSkillAddPhysicalAttack;
	}



	public double getTotalSkillAddPhysicalAttackSpeed() {
		this.totalSkillAddPhysicalAttackSpeed = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddPhysicalAttackSpeed += skills[i].getAddPhysicalAttackSpeed();
		}
		return this.totalSkillAddPhysicalAttackSpeed;
	}



	public void setTotalSkillAddPhysicalAttackSpeed(
			double totalSkillAddPhysicalAttackSpeed) {
		this.totalSkillAddPhysicalAttackSpeed = totalSkillAddPhysicalAttackSpeed;
	}



	public int getTotalSkillAddPhysicalAttackArea() {
		this.totalSkillAddPhysicalAttackArea = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddPhysicalAttackArea += skills[i].getAddPhysicalAttackArea();
		}
		return this.totalSkillAddPhysicalAttackArea;
	}



	public void setTotalSkillAddPhysicalAttackArea(
			int totalSkillAddPhysicalAttackArea) {
		this.totalSkillAddPhysicalAttackArea = totalSkillAddPhysicalAttackArea;
	}



	public int getTotalSkillAddMovementSpeed() {
		this.totalSkillAddMovementSpeed = 0;
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++) {
			if (this.skills[i] != null)
				if (this.skills[i].getSkillType() == 5 && this.skills[i].getSkillLevel() > 0)
					this.totalSkillAddMovementSpeed += skills[i].getAddMovementSpeed();
		}
		return this.totalSkillAddMovementSpeed;
	}



	public void setTotalSkillAddMovementSpeed(int totalSkillAddMovementSpeed) {
		this.totalSkillAddMovementSpeed = totalSkillAddMovementSpeed;
	}



	// add in a skill
	public void addSkill(Skill skill){
		for (int i=0; i<GameFrame.MAX_SKILL_NUMBER; i++){
			if(this.skills[i] == null){
				this.skills[i] = new Skill(skill);
				GameFrame.allCharacterInfoGameButtons.get(17 + i).setImage(skill.getImage().getImage());
				System.out.println("Add a new skill " + skill.getSkillName() + " to list!");
				break;
			}
		}
	}



	public int getUnusedSkillCount() {
		return unusedSkillCount;
	}



	public void setUnusedSkillCount(int unusedSkillCount) {
		if (unusedSkillCount < 0) {
			System.out.println("unusedSkillCount cannnot go below 0!");
		} else {
			this.unusedSkillCount = unusedSkillCount;
		}
	}



	public static void addPerTurnMoney(Hero hero) {
		// add money to hero's account every round starting from round 10 onwards
		if (GameFrame.turn >= 10) {
			hero.setMoney(hero.getMoney() + HERO_PER_ROUND_MONEY_INCREASE);
		}
	}

}

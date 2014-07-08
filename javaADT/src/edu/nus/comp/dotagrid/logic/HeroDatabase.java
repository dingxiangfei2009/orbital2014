package edu.nus.comp.dotagrid.logic;


public class HeroDatabase {
	
	public static int totalHeroNumber = 1;
	
	public Hero[] heroDatabase = new Hero[totalHeroNumber];
	
	public HeroDatabase(){
		
		/* 
		 * String heroName, 
		 * int BountyMoney
		 * int bountyExp
		 * int sight
		 * 
		 * int heroStartingMoney
		 * String mainAttribute
		 * 
		 * int startingHP, 
		 * int startingMP, 
		 * int startingPhysicalAttack, 
		 * int startingPhysicalAttackArea, 
		 * double startingPhysicalAttackSpeed, 
		 * double startingPhysicalDefense, 
		 * double startingMagicResistance, 
		 * int actionPoint,
		 * int teamNumber
		 * 
		 * int strength, 
		 * int agility, 
		 * int intelligence, 
		 * double strengthGrowth, 
		 * double agilityGrowth, 
		 * double intelligenceGrowth,
		 * int movementSpeed
		 * 
		 * */
		
		Hero fur = new Hero("fur", "intelligence", 
							150, 10, 30000, 3, 0.7, 3.52, 20, 100, 1,
							19, 18, 21, 1.8, 1.9, 2.9, 295);	
		fur.setCharacterImage("heroes", "fur");
		fur.addSkill(SkillDatabase.skillDatabase[0]);
		fur.addSkill(SkillDatabase.skillDatabase[1]);
		heroDatabase[0] = fur;
		
	}

}

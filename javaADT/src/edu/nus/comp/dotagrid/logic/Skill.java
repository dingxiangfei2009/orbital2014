package edu.nus.comp.dotagrid.logic;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Skill {
	
	public static int invokedSkillType = -1;
	
	/*
	 * Skill type : 
	 * 
	 * 1 : teleport
	 * 
	 */
	
	private ImageIcon skillImage;
	
	private int skillType;
	private String skillName;
	
	private int usedMP;
	private int usedActionPoint;
	
	private int castRange;
	
	// coolDownRounds records the minimum rounds, if a hero has casted this spell, needed to end before using this skill again
	// currentRound records the rounds needed to end before using this skill again
	private int coolDownRounds, currentCoolDownRound;
	
	
	// constructor
	public Skill(int skillType, String skilName, int[] attributes) {
		this.skillType = skillType;
		this.skillName = skilName;
		this.setImage();
		
		switch(this.skillType) {
		
			case 1 :
				// usedMP, usedActionPoint, castRange, coolDownRounds
				this.usedMP = attributes[0];
				this.usedActionPoint = attributes[1];
				this.castRange = attributes[2];
				this.coolDownRounds = attributes[3];
				this.setCurrentCoolDownRound(0);
				break;
				
			case 2 :
				
				break;
		}
		

	}
	
	
	
	public Skill(Skill skill) {
		
		this.setSkillType(skill.getSkillType());
		this.setSkillName(skill.getSkillName());
		this.setImage();

		this.setUsedMP(skill.getUsedMP());
		this.setUsedActionPoint(skill.getUsedActionPoint());
		this.setCastRange(skill.getCastRange());
		
		this.setCoolDownRounds(skill.getCoolDownRounds());
		this.setCurrentCoolDownRound(skill.getCurrentCoolDownRound());
	}



	public int getSkillType() {
		return skillType;
	}


	public void setSkillType(int skillType) {
		this.skillType = skillType;
	}


	public int getUsedMP() {
		return usedMP;
	}


	public void setUsedMP(int usedMP) {
		this.usedMP = usedMP;
	}


	public int getUsedActionPoint() {
		return usedActionPoint;
	}


	public void setUsedActionPoint(int usedActionPoint) {
		this.usedActionPoint = usedActionPoint;
	}


	public int getCastRange() {
		return castRange;
	}


	public void setCastRange(int castRange) {
		this.castRange = castRange;
	}


	public int getCoolDownRounds() {
		return coolDownRounds;
	}


	public void setCoolDownRounds(int coolDownRounds) {
		this.coolDownRounds = coolDownRounds;
	}



	public int getCurrentCoolDownRound() {
		return currentCoolDownRound;
	}



	public void setCurrentCoolDownRound(int currentRound) {
		if (currentRound <= 0) {
			this.currentCoolDownRound = 0;
		} else {
			this.currentCoolDownRound = currentRound;
		}
	}



	public String getSkillName() {
		return skillName;
	}



	public void setSkillName(String skilName) {
		this.skillName = skilName;
	}



	public ImageIcon getImage() {
		return skillImage;
	}

	public void setImage() {
		this.skillImage = new ImageIcon("res/skills/" + skillName + ".gif");
	}



	public void invokeSkillAction(int heroXPos, int heroYPos) {
		
		// reset attack map and highlighted map
		for (int x=0; x<GridFrame.ROW_NUMBER; x++) {
			for (int y=0; y<GridFrame.COLUMN_NUMBER; y++) { 
				GridFrame.highlightedMap[x][y] = -1;
				GridFrame.attackRangeMap[x][y] = -1;
			}
		}	
					
		// change highlighted map
		for(int x=heroXPos-this.getCastRange(); x<heroXPos+this.getCastRange()+1; x++){
			for(int y=heroYPos-this.getCastRange(); y<heroYPos+this.getCastRange()+1; y++){
				// x and y need to be within the grid frame 
				if (x >= 0 && x <= GridFrame.COLUMN_NUMBER-1){
					if (y>=0 && y <= GridFrame.ROW_NUMBER-1) {
						// x + y need to be within the number of attackable grid
						if (Math.abs(heroXPos - x) + Math.abs(heroYPos - y) <= this.getCastRange()) {
							GridFrame.highlightedMap[x][y] = 1;
						}
					}
				}
			}
		}

	}
}

package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.Packable;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONObject;

/**
 * Created by mike on 11.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class CustomMob extends MultiKindMob implements IZapper {

	private final String MOB_CLASS = "mobClass";

	@Packable
	private String luaData;

	private int dmgMin, dmgMax;
	private int attackSkill;
	private int dr;

	private float attackDelay = 1;

	private String mobClass = "Unknown";

	private boolean canBePet = false;

	private int     attackRange     = 1;

	private boolean friendly;

	//For restoreFromBundle
	public CustomMob() {
	}

	public CustomMob(String mobClass) {
		this.mobClass = mobClass;
		fillMobStats(false);
	}

	@Override
	protected float attackDelay() {
		return attackDelay;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(dmgMin, dmgMax);
	}

	@Override
	public int attackSkill(Char target) {
		return attackSkill;
	}

	@Override
	public int dr() {
		return dr;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);

		bundle.put(MOB_CLASS, mobClass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		mobClass = bundle.getString(MOB_CLASS);
		fillMobStats(true);

		super.restoreFromBundle(bundle);
	}

	@Override
	protected void readCharData() {
		super.readCharData();
	}

	@Override
	public String getMobClassName() {
		return mobClass;
	}

	@Override
	public boolean canBePet() {
		return canBePet;
	}

	@Override
	protected boolean canAttack( Char enemy ) {

		if(friendly(enemy)) {
			return false;
		}

		int enemyPos = enemy.getPos();
		int distance = Dungeon.level.distance(getPos(), enemyPos);

		if(distance <= attackRange && Ballistica.cast(getPos(), enemyPos, false, true) == enemyPos) {
			return true;
		}

		return false;
	}

	@Override
	public boolean friendly(Char chr) {
		return friendly || super.friendly(chr);
	}

	public void setData(String data) {
		luaData = data;
	}

	public String getData() {
		return luaData;
	}

	private void fillMobStats(boolean restoring) {
		try {
			JSONObject classDesc = getClassDef();

			defenseSkill = classDesc.optInt("defenseSkill", defenseSkill);
			attackSkill = classDesc.optInt("attackSkill", attackSkill);

			exp = classDesc.optInt("exp", exp);
			maxLvl = classDesc.optInt("maxLvl", maxLvl);
			dmgMin = classDesc.optInt("dmgMin", dmgMin);
			dmgMax = classDesc.optInt("dmgMax", dmgMax);

			dr = classDesc.optInt("dr", dr);

			baseSpeed = (float) classDesc.optDouble("baseSpeed", baseSpeed);
			attackDelay = (float) classDesc.optDouble("attackDelay", attackDelay);

			name = StringsManager.maybeId(classDesc.optString("name", mobClass+"_Name"));
			name_objective = StringsManager.maybeId(classDesc.optString("name_objective", mobClass+"_Name_Objective"));
			description = StringsManager.maybeId(classDesc.optString("description", mobClass+"_Desc"));
			gender = Utils.genderFromString(classDesc.optString("gender", mobClass+"_Gender"));

			spriteClass = classDesc.optString("spriteDesc", "spritesDesc/Rat.json");

			flying = classDesc.optBoolean("flying", flying);

			lootChance = (float) classDesc.optDouble("lootChance", lootChance);

			if (classDesc.has("loot")) {
				loot = ItemFactory.createItemFromDesc(classDesc.getJSONObject("loot"));
			}

			viewDistance = classDesc.optInt("viewDistance",viewDistance);
			viewDistance = Math.min(viewDistance, ShadowCaster.MAX_DISTANCE);

			walkingType = Enum.valueOf(WalkingType.class, classDesc.optString("walkingType","NORMAL"));

			defenceVerb = StringsManager.maybeId(classDesc.optString("defenceVerb", Game.getVars(R.array.Char_StaDodged)[gender]));

			canBePet = classDesc.optBoolean("canBePet",canBePet);

			attackRange = classDesc.optInt("attackRange",attackRange);

			scriptFile = classDesc.optString("scriptFile", scriptFile);

			if(!restoring) {
				setFraction(Enum.valueOf(Fraction.class, classDesc.optString("fraction","DUNGEON")));
				friendly = classDesc.optBoolean("friendly",friendly);
				hp(ht(classDesc.optInt("ht", 1)));
				fromJson(classDesc);
			}

			runMobScript("fillStats");

		} catch (Exception e) {
			throw new TrackedRuntimeException(e);
		}
	}

}

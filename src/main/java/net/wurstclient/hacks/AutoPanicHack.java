package net.wurstclient.hacks;

import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"legit", "disable"})
public final class AutoPanicHack extends Hack implements UpdateListener
{
	private final SliderSetting detectionRangeSlider = new SliderSetting(
		"Detection range",
		"Determines how close another player has to be to auto panic.\n"
			+ "Any player closer than the specified value will trigger auto panic.",
		128, 0, 512, 1, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting dontPanicNearFriendsCheckbox =
		new CheckboxSetting("Don't auto panic near friends",
			"If set to true, players within the detection range who are on your friends list will not trigger auto panic.",
			false);
	
	public AutoPanicHack()
	{
		super("AutoPanic");
		setCategory(Category.OTHER);
		
		addSetting(detectionRangeSlider);
		addSetting(dontPanicNearFriendsCheckbox);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// Get the values of the settings
		boolean dontPanicNearFriends = dontPanicNearFriendsCheckbox.isChecked();
		double detectionRangeSq = Math.pow(detectionRangeSlider.getValue(), 2);
		
		// Get a stream of nearby enemy players that might detect us
		Stream<Entity> stream = EntityUtils.getOtherPlayers();
		stream = stream
			.filter(e -> MC.player.squaredDistanceTo(e) <= detectionRangeSq);
		if(dontPanicNearFriends)
		{
			stream = stream.filter(e -> !WURST.getFriends().isFriend(e));
		}
		
		// Panic if there's nearby enemy players
		if(stream.count() > 0)
		{
			panic();
		}
	}
	
	private void panic()
	{
		for(Hack hack : WURST.getHax().getAllHax())
			if(hack.isEnabled() && hack != this)
				hack.setEnabled(false);
		setEnabled(false);
	}
}

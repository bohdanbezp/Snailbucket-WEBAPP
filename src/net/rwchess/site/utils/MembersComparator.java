package net.rwchess.site.utils;

import java.text.Collator;
import java.util.Comparator;

public class MembersComparator implements Comparator<UsernameComparable> {
	
	private final Collator c = Collator.getInstance();
	
	private static MembersComparator mc;
	
	private MembersComparator() {}
	
	public static MembersComparator getInstance() {
		if (mc == null)
			mc = new MembersComparator();
		
		return mc;
	}

	@Override
	public int compare(UsernameComparable m1, UsernameComparable m2) {
		String s1 = m1.getUsername().toLowerCase();
		String s2 = m2.getUsername().toLowerCase();
		return c.compare(s1, s2);
	}

}

package net.rwchess.persistent.dao;

import net.rwchess.persistent.Member;

import java.util.List;

/**
 * Created by bodia on 10/12/14.
 */
public interface MemberDAO {
    public Member getMemberByUsername(String username);

    public Member getMemberById(Long key);

    public void toggleConfirmed(String username);

    public void store(Member member);

    public List<Member> getAllConfirmedMembers();

    public List<Member> getAllMembers();

    public void updateWithData(String username, String passwordHash, String country, String badTimes, String hardTimes, String timeControlPreferrence, String email);

    public void updateRole(Long key, int newGroup);

    public void updateRR(Long key, int rr);

    public void updatePassword(Long key, String password);

    public void updateTimeorder(Long key, String timeOrder);

    public void updateInsist(Long key, String badTimes, String hardTimes);
    
    public InsistData getInsistData(String insist);
}

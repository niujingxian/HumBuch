package de.dhbw.humbuch.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="student")
public class Student implements de.dhbw.humbuch.model.entity.Entity {
	
	@Id
	private int id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="gradeId", referencedColumnName="id")
	private Grade grade;
	private String lastname;
	private String firstname;
	private Date birthday;
	private String gender;
	
	@OneToMany(mappedBy="student", fetch=FetchType.LAZY)
	private List<BorrowedMaterial> borrowedList = new ArrayList<BorrowedMaterial>();

	@ElementCollection(targetClass=ProfileType.class)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name="studentProfile", joinColumns = @JoinColumn(name="studentId"))
	@Column(name="profileType")
	private Set<ProfileType> profileTypes;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="parentId")
	private Parent parent;
	
	public Student() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Parent getParent() {
		return parent;
	}

	public void setParent(Parent parent) {
		this.parent = parent;
	}

	public List<BorrowedMaterial> getBorrowedList() {
		return borrowedList;
	}

	public void setBorrowedList(List<BorrowedMaterial> borrowedList) {
		this.borrowedList = borrowedList;
	}

	public Set<ProfileType> getProfileTypes() {
		return profileTypes;
	}

	public void setProfileTypes(Set<ProfileType> profileTypes) {
		this.profileTypes = profileTypes;
	}
	
}

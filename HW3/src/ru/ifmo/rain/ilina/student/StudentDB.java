package ru.ifmo.rain.ilina.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {
    private static final Comparator<Student> nameCmp = Comparator.comparing(Student::getLastName, String::compareTo)
            .thenComparing(Student::getFirstName, String::compareTo)
            .thenComparingInt(Student::getId);

    private <T extends Collection<String>> T mappedStudentsCollection(List<Student> students, Function<Student, String> mapper, Supplier<T> collection) {
        return students.stream().map(mapper).collect(Collectors.toCollection(collection));
    }

    private List<String> mappedStudentsList(List<Student> students, Function<Student, String> mapper) {
        return mappedStudentsCollection(students, mapper, ArrayList::new);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mappedStudentsList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mappedStudentsList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mappedStudentsList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mappedStudentsList(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mappedStudentsCollection(students, Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");

    }

    ///

    private List<Student> sortedStudents(Stream<Student> studentStream, Comparator<Student> cmp) {
        return studentStream.sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedStudents(students.stream(), Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedStudents(students.stream(), nameCmp);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return students.stream().sorted(nameCmp).flatMap(
                x -> name.equals(x.getFirstName()) ? Stream.of(x) : Stream.of()
        ).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return students.stream().sorted(nameCmp).flatMap(
                x -> name.equals(x.getLastName()) ? Stream.of(x) : Stream.of()
        ).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return students.stream().sorted(nameCmp).flatMap(
                x -> group.equals(x.getGroup()) ? Stream.of(x) : Stream.of()
        ).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {//todo:: убивает всех студентов из одной группы  с одинаковой фамилией, оставляя только одного с минимальным именем
        return students.stream().sorted(nameCmp).flatMap(
                x -> group.equals(x.getGroup()) ? Stream.of(x) : Stream.of())
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    ///

    private Stream<Entry<String, List<Student>>> getSortedGroupsStream(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> getSortedGroups(Stream<Entry<String, List<Student>>> groupsStream, UnaryOperator<List<Student>> sorter) {//todo::
        return groupsStream.map(elem -> new Group(elem.getKey(), sorter.apply(elem.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {//todo::
        return getSortedGroups(getSortedGroupsStream(students), this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroups(getSortedGroupsStream(students), this::sortStudentsById);
    }

    private String getFilteredLargestGroup(Stream<Entry<String, List<Student>>> groupsStream, ToIntFunction<List<Student>> f) {
        return groupsStream
                .max(Comparator.comparingInt((Entry<String, List<Student>> group) -> f.applyAsInt(group.getValue()))
                        .thenComparing(Entry::getKey, Collections.reverseOrder(String::compareTo)))
                .map(Entry::getKey).orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getFilteredLargestGroup(getSortedGroupsStream(students), List::size);

    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getFilteredLargestGroup(getSortedGroupsStream(students), studentsList -> getDistinctFirstNames(studentsList).size());
    }
}
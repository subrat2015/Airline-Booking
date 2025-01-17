package com.subrat.checkin.dao;

import com.subrat.Main;
import com.subrat.checkin.db.PoolManager;
import com.subrat.checkin.dto.Seat;
import com.subrat.checkin.dto.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatsRepository {

    private static final PoolManager poolManager = Main.getPoolManager();
    private static final String UNASSIGNED_SEAT_QUERY = "SELECT * FROM seats WHERE user_id is null order by id LIMIT 1";

    public static List<Seat> findAll() {
        List<Seat> seats = new ArrayList<>();
        Connection connection = poolManager.getConnection("mysql");
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM seats");) {
            while (resultSet.next()) {
                Seat seat = new Seat();
                seat.setId(resultSet.getInt("id"));
                seat.setName(resultSet.getString("name"));
                seat.setFlightId(resultSet.getInt("flight_id"));
                seat.setUserId(resultSet.getInt("user_id"));
                seats.add(seat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            poolManager.releaseConnection("mysql", connection);
        }
        return seats;
    }

    public static Seat bookSeat(User user) {
        System.out.println("Booking seat for user " + user.getId() + ":" + user.getName() + " by thread: " + Thread.currentThread().getName());

        String unassignedSeatQuery = "SELECT * FROM seats WHERE user_id is null order by id LIMIT 1 FOR UPDATE SKIP LOCKED";
        String updateSeatQuery = "UPDATE seats SET user_id = ? WHERE id = ?";

        ResultSet resultSet = null;
        Seat seat = null;

        Connection connection = poolManager.getConnection("mysql");

        try (PreparedStatement unassignedSeatStatement = connection.prepareStatement(unassignedSeatQuery);
             PreparedStatement updateSeatStatement = connection.prepareStatement(updateSeatQuery)) {
            // begin transaction
            connection.setAutoCommit(false);
            // execute 1. unassigned seat query
            if (unassignedSeatStatement.execute()) {
                resultSet = unassignedSeatStatement.getResultSet();
                if (resultSet.next()) {
                    seat = new Seat();
                    seat.setId(resultSet.getInt("id"));
                    seat.setName(resultSet.getString("name"));
                    seat.setFlightId(resultSet.getInt("flight_id"));
                    seat.setUserId(user.getId());

                    updateSeatStatement.setInt(1, user.getId());
                    updateSeatStatement.setInt(2, seat.getId());
                    // execute 2. update seat query
                    updateSeatStatement.executeUpdate();
                } else {
                    System.out.println("No unassigned seats found");
                }
            }
            connection.commit();
            // end transaction
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    poolManager.releaseConnection("mysql", connection);
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return seat;
    }


    public static void save(Seat seat) {
        System.out.println("Saving seat by thread: " + Thread.currentThread().getName());
        Connection connection = poolManager.getConnection("mysql");
        try (
                Statement statement = connection.createStatement();) {
            String query = "UPDATE seats SET user_id = " + seat.getUserId() + " WHERE id = " + seat.getId();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            poolManager.releaseConnection("mysql", connection);
        }
    }

    public static Seat findUnassignedSeat() {
        System.out.println("Finding unassigned seat by thread: " + Thread.currentThread().getName());
        Seat seat = new Seat();
        Connection connection = poolManager.getConnection("mysql");
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(UNASSIGNED_SEAT_QUERY);) {
            while (resultSet.next()) {
                seat.setId(resultSet.getInt("id"));
                seat.setName(resultSet.getString("name"));
                seat.setFlightId(resultSet.getInt("flight_id"));
                seat.setUserId(resultSet.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            poolManager.releaseConnection("mysql", connection);
        }
        return seat;
    }

    public static void clearAllSeats() {
        Connection connection = poolManager.getConnection("mysql");
        try (
                Statement statement = connection.createStatement();) {
            String query = "UPDATE seats SET user_id = null";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            poolManager.releaseConnection("mysql", connection);
        }
    }
}

DELIMITER //

CREATE PROCEDURE add_movie (
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32),
    IN birth_year INT
)
BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);
    DECLARE max_movie_number INT;
    DECLARE max_star_number INT;

    -- Find the maximum number used in existing movie IDs
    SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO max_movie_number FROM movies;

    -- Find the maximum number used in existing star IDs
    SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO max_star_number FROM stars;

    -- Increment the maximum number by 1 to get the new number
    SET max_movie_number = IFNULL(max_movie_number, 0) + 1;
    SET max_star_number = IFNULL(max_star_number, 0) + 1;

    -- Generate the new movie ID
    SELECT id into movie_id from movies where title = movie_title and year = movie_year LIMIT 1;

    IF movie_id IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Movie already exists';
    END IF;

    SET movie_id = CONCAT('tt', LPAD(max_movie_number, 7, '0'));

    SELECT id into star_id from stars where name = star_name and birthYear = birth_year LIMIT 1;

    -- Check if the genre exists, if not, insert it
    SELECT id INTO genre_id FROM genres WHERE name = genre_name LIMIT 1;

    IF star_id IS NULL THEN
        -- Generate the new star ID
        SET star_id = CONCAT('nm', LPAD(max_star_number, 7, '0'));
        INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, birth_year);
    END IF;

    IF genre_id IS NULL THEN
        -- If the genre doesn't exist, insert it
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
    END IF;

    -- Insert the movie
    INSERT INTO movies (id, title, year, director) VALUES (movie_id, movie_title, movie_year, movie_director);

    -- Link the star to the movie
    INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);

    -- Link the genre to the movie
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_star (
    IN star_name VARCHAR(100),
    IN birth_year INT
)
BEGIN
    DECLARE star_id VARCHAR(10);
    DECLARE max_star_number INT;

    -- Find the maximum number used in existing star IDs
    SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) INTO max_star_number FROM stars;

    -- Increment the maximum number by 1 to get the new number
    SET max_star_number = IFNULL(max_star_number, 0) + 1;

    -- Generate the new star ID
    SET star_id = CONCAT('nm', LPAD(max_star_number, 7, '0'));

    -- Insert the star
    INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, birth_year);
END //

DELIMITER ;


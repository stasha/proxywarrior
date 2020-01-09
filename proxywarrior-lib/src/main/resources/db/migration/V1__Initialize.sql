CREATE CACHED TABLE CONFIGURATION (
    ID INTEGER IDENTITY PRIMARY KEY,
    CONFIG_ID VARCHAR(200) UNIQUE NOT NULL,
    LAST_CONFIG_USED BOOLEAN NOT NULL,
    CONFIG_PATH VARCHAR(200),
    CONFIG VARCHAR(10M) NOT NULL
);

CREATE CACHED TABLE REQUEST (
    ID VARCHAR(50) PRIMARY KEY,
    CONFIGURATION_ID INTEGER FOREIGN KEY REFERENCES CONFIGURATION(ID),
    REQUEST_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    REQUEST_URL VARCHAR(1K) NOT NULL,
    PROXY_URL VARCHAR(1K) NOT NULL,
    REQUEST_METHOD VARCHAR(10) NOT NULL,
    REQUEST_HEADERS VARCHAR(10K) NOT NULL,
    REQUEST_CONTENT BLOB
);

CREATE CACHED TABLE PROXY_REQUEST (
    REQUEST_ID VARCHAR(50) PRIMARY KEY FOREIGN KEY REFERENCES REQUEST(ID),
    PROXY_REQUEST_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PROXY_REQUEST_HEADERS VARCHAR(10K) NOT NULL
)

CREATE CACHED TABLE PROXY_RESPONSE (
    REQUEST_ID VARCHAR(50) PRIMARY KEY FOREIGN KEY REFERENCES REQUEST(ID),
    PROXY_RESPONSE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PROXY_RESPONSE_STATUS_CODE INTEGER NOT NULL,
    PROXY_RESPONSE_STATUS_LINE VARCHAR(1024) NOT NULL,
    PROXY_RESPONSE_HEADERS VARCHAR(10K) NOT NULL,
    PROXY_RESPONSE_CONTENT BLOB
)

CREATE CACHED TABLE RESPONSE (
    REQUEST_ID VARCHAR(50) PRIMARY KEY FOREIGN KEY REFERENCES REQUEST(ID),
    RESPONSE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    RESPONSE_HEADERS VARCHAR(10K) NOT NULL
)
package com.zo.config

import slick.jdbc.{JdbcProfile, MySQLProfile}

trait DB {
    
    val driver: JdbcProfile
    
    import driver.api._
    
    lazy val db: Database = Database.forConfig("database")
}

trait MSQL extends DB {
    
    override val driver = MySQLProfile
}



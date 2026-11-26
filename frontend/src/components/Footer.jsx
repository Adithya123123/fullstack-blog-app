import React from 'react';

/**
 * Footer Component.
 */
function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div>
          <strong>DevLog</strong> &mdash; Built with &#10084;&#65039; by <strong>Adithya</strong> &amp; <strong>Seela Nandini</strong> | B.Tech CSE Portfolio
        </div>
        <div className="footer-tech">
          <span className="tech-tag">Java 17</span>
          <span className="tech-tag">JDBC</span>
          <span className="tech-tag">HttpServer</span>
          <span className="tech-tag">MySQL</span>
          <span className="tech-tag">React</span>
          <span className="tech-tag">Vite</span>
        </div>
      </div>
    </footer>
  );
}

export default Footer;

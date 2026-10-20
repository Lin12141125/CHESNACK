CREATE DATABASE IF NOT EXISTS chesnake;
use chesnake;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` varchar(50) NOT NULL,
  `type` varchar(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `origin` varchar(20) NOT NULL,
  `brand` varchar(50) NOT NULL,
  `net_content` int NOT NULL,
  `price` decimal(18,2) NOT NULL,
  `sale_quantity` int NOT NULL,
  `img1` varchar(500) DEFAULT NULL,
  `img2` varchar(500) DEFAULT NULL,
  `score` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;



INSERT INTO `goods` VALUES ('1', 'dried meat', 'Natural slices of pork breast made with herb monosodium glutamate', 'Sichuan province', 'BE&CHEERY', '100', '8.00', '10000', '1.jpg', '2.jpg', '0');
INSERT INTO `goods` VALUES ('10', 'chili', 'North and South special torn vegetarian meat bean dry spicy strip vegetarian beef steak products Internet celebrity snacks snack snack snack food Year L', 'Hunan province', 'NBT/ North and South Te', '50', '5.00', '300', '19.jpg_.webp', '20.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('11', 'chili', 'Spicy mushroom bean dry snack snack bean curd dry spicy bar casual snack wholesale small package', 'Shandong province', 'Hai Yueyuan', '35', '14.00', '200', '21.jpg_.webp', '22.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('12', 'confectionery', 'Pumpkin seed crisp 500g old traditional handmade pastry tea specialty office casual snack pumpkin seed sugar', 'Zhejiang Province', 'Yamato', '250', '14.00', '400', '23.jpg_.webp', '24.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('13', 'bread', 'Xiamen coconut cake nutrition breakfast pastry coconut bread snack food to satisfy the craving for specialty snacks network red small snacks', 'Fujian Province', ' Countless', '300', '8.00', '1000', '25.jpg_.webp', '26.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('14', 'bread', 'Hongyi white toast sliced bread Whole box healthy breakfast lazy quick snack snack snack snack snack food', 'Fujian Province', 'Rainy', '400', '9.00', '800', '27.jpg_.webp', '28.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('15', 'meat', 'Beef stick instant beef jerky bar evening snack Reduce packaged meat deli fat casual snack', 'Guangdong province', 'YOME’S/Yumez', '860', '14.00', '1000', '29.jpg_.webp', '30.webp', '0');
INSERT INTO `goods` VALUES ('16', 'meat', 'Bibizan small fish small fish dried to satisfy the craving spicy seafood snacks snack food snacks ready-to-eat wine dishes', 'Hunan province', 'Opera', '70', '10.00', '1000', '31.webp', '32.webp', '0');
INSERT INTO `goods` VALUES ('17', 'bread', 'Yogurt-flavored hemp flower bread whole box of breakfast pastry heart to satisfy hunger late night snack snacks snack food wholesale', 'Fujian Province', 'Opera', '300', '7.00', '75', '33.webp', '34.webp', '0');
INSERT INTO `goods` VALUES ('18', 'chili', 'Sichuan Grandma sandwich kelp crispy mushroom bean skin roll net celebrity foodie snack office snack snack', 'Shandong province', 'Stuffed with crispy mushrooms and kelp', '500', '12.00', '300', '35.webp', '36.webp', '0');
INSERT INTO `goods` VALUES ('19', 'nut', 'Padan wood melon seeds crisp sugar fruit sunflower seeds mixed nuts snack individual small package snack food', 'Fujian province', 'BUH', '50', '2.00', '6000', '37.webp', '38.webp', '0');
INSERT INTO `goods` VALUES ('2', 'puffed food', 'Want Want small crisp delicious nostalgic snack gift package to satisfy cravings net red puffed casual snack food small package', 'Henan province', 'Tao Food Style', '200', '15.00', '10000', '3.jpg', '4.png', '0');
INSERT INTO `goods` VALUES ('20', 'candy', 'ango dry milk cake cow zag sugar love crisp cheese stick office children drama snacks snack snack food', 'Anhui province', 'Fancy delicacy', '125', '13.00', '1000', '39.webp', '40.webp', '0');
INSERT INTO `goods` VALUES ('3', 'chili', 'Yanjin shop spicy bar snack casual snack food network red burst spicy bar gift package dormitory to satisfy hunger', 'Hunan province', 'yanjin shop', '72', '5.00', '10000', '5.jpg_.webp', '6.webp', '0');
INSERT INTO `goods` VALUES ('4', 'puffed food', 'Bibizan palm dry noodles eat mixed Turkey crispy noodles full box bag instant noodles', 'Fujian Province', 'BGRIMM', '420', '9.00', '1000', '7.jpg_.webp', '8.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('5', 'candy', 'Vietnam import Ben Ben Ben Ben Ben Ben Ben cocoa special concentrated coconut sugar old packaging Hainan specialty childhood nostalgia snack', 'Hunan province', 'Ben Tre', '400', '13.00', '2000', '9.jpg_.webp', '10.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('6', 'confectionery', 'Huaishan Coix barley pastry heart full low-fat wheat coarse grain bread breakfast whole box of non-saccharin snack zero food', 'Hunan province', 'Dieva yunnanensis', '25', '5.00', '400', '11.jpg_.webp', '12.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('7', 'bread', 'Free delivery Fortune More hand-torn bread 400g*1 box full box meal replacement cake casual snacks hand-torn snacks', 'Fujian Province', 'Have many blessings', '40', '8.00', '400', '13.jpg_.webp', '14.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('8', 'nut', 'Orchid bean broad bean snacks casual snack food beef taste to satisfy the greedy small package of wine and vegetables fried goods whole box wholesale', 'Anhui province', 'Fancy delicacy', '500', '9.00', '1000', '15.jpg_.webp', '16.jpg_.webp', '0');
INSERT INTO `goods` VALUES ('9', 'bread', 'Thousands of silk crispy brown sugar twist net red to satisfy the craving biscuit pouch breakfast bread snack snack casual explosive', 'Fujian province', 'Small twist 6', '500', '8.00', '1000', '17.jpg_400x400.webp', '18.jpg_400x400.webp', '0');

INSERT INTO `goods` VALUES ('21', 'bread', 'No second-hand ripped milk cube bread', 'Fujian Province', 'love castle', '700', '59.90', '2', '41.webp', '42.webp', '0');
INSERT INTO `goods` VALUES ('22', 'bread', 'Panpan French small bread nutrition breakfast pastries stock anti hunger satiating hunger full night snack leisure health zero food', 'Fujian Province', 'Panpan', '250', '7.60', '1000', '43.webp', '44.webp', '0');
INSERT INTO `goods` VALUES ('23', 'bread', 'Daly Garden Chocolate Pie 2500g breakfast bread Zero food biscuits Full box egg yolk pie pastry gourmet snacks', 'Hubei Province', 'Daliyuan', '500', '14.00', '100', '45.webp', '46.webp', '0');
INSERT INTO `goods` VALUES ('24', 'bread', 'Yanjin Shop Lactic acid bacteria small pocket bread Whole box mango pineapple cake snack snacks Healthy breakfast food', 'Hunan province', 'yanjin shop', '100', '20.00', '2000', '47.webp', '48.webp', '0');
INSERT INTO `goods` VALUES ('25', 'bread', 'Blueberry Rock baked toast filled with cheese bread full box cake breakfast nutritious Dragon Boat Festival snacks casual snacks', 'Anhui Province', 'Fancy delicacy', '250', '10.00', '300', '49.webp', '50.webp', '0');
INSERT INTO `goods` VALUES ('26', 'chocolate', 'Nuts and peanuts filled chocolate energy bar Snickers bar wholesale New Year candy candy to satisfy hunger small snacks', 'shandong province', 'Zhu Xiaoer', '500', '9.00', '1000', '51.webp', '52.webp', '0');
INSERT INTO `goods` VALUES ('27', 'chocolate', 'Dove Chocolate Silky Milk Black Chocolate Pure cocoa butter with Hand Gift candy casual snacks loose Christmas', 'Beijing', 'Dove', '112', '10.80', '2000', '53.webp', '54.webp', '0');
INSERT INTO `goods` VALUES ('28', 'chocolate', 'Bibizan small black sandwich cookies chocolate whole box of bulk office snacks snack snack food Internet celebrity', 'Fujian Province', ' BGRIMM', '400', '7.00', '3000', '55.webp', '56.webp', '0');
INSERT INTO `goods` VALUES ('29', 'chocolate', 'Indonesia imported Ritz Nabotti soft heart Brownies Lostar chocolate chip cookies small food', 'Indonesia', 'Richeese', '560', '15.00', '1000', '57.webp', '58.webp', '0');
INSERT INTO `goods` VALUES ('30', 'chocolate', 'Cookies Cranberries chocolate grout night snack Internet celebrity burst individual small package flagship store', 'shandong province', 'pyrinone', '12', '13.68', '2000', '59.webp', '60.webp', '0');
INSERT INTO `goods` VALUES ('31', 'biscuits', 'Ryopin Shop Tensun Salt small round cake 102gx10 bags of Japanese crispy biscuits salty snack breakfast for children', 'Guangdong province', 'Bestore', '1020', '10.00', '300', '61.webp', '62.webp', '0');
INSERT INTO `goods` VALUES ('32', 'nut', 'Good product shop beef flavor orchid bean 120gx3 bag broad bean crispy packaging snack food Hu bean seed', 'Anhui Province', 'Bestore', '360', '17.80', '700', '63.webp', '64.webp', '0');
INSERT INTO `goods` VALUES ('33', 'nut', 'Good shop dried fruit snack gift pack with preserved fruit Mango strawberry pineapple dried casual snack', 'Hubei Province', 'Bestore', '306', '29.80', '79', '65.webp', '66.webp', '0');
INSERT INTO `goods` VALUES ('34', 'chili', 'Good food shop baked gluten Qiao bean skin small roast spicy spicy strip bag store the same packaged snacks', 'Hunan province', 'Bestore', '15', '18.60', '1000', '67.webp', '68.webp', '0');
INSERT INTO `goods` VALUES ('35', 'nut', 'Good product shop Fructus Trichosanthis seed Milk fragrance 19 packs about 500g store independent pouch melon seed snack food', 'Jiangsu province', 'Bestore', '250', '26.80', '15', '69.webp', '70.webp', '0');
INSERT INTO `goods` VALUES ('36', 'biscuits', 'Oreo 40g*6 boxes of mini cocoa Crisp Heart cookies Casual chocolate stuffed snacks with many flavors', 'Shanghai', 'OREO', '40', '18.90', '10', '71.webp', '72.webp', '0');
INSERT INTO `goods` VALUES ('37', 'biscuits', 'Olio sandwich cookies chocolate snack food multi-flavor snacks snacks bulk 500g about 25 packs', 'Jiangsu province', 'OREO', '77', '29.80', '4000', '73.webp', '74.webp', '0');
INSERT INTO `goods` VALUES ('38', 'biscuits', 'Mondelez Oreo Chocolate Bar Cocoa Bar Chocolate Bar Wafer 500g*2 bags of mixed flavor snacks', 'Beijing', 'OREO', '500', '22.80', '700', '75.webp', '76.webp', '0');
INSERT INTO `goods` VALUES ('39', 'biscuits', 'mico mini Oreo Mini Sandwich cookie small round snack macaron mixed small black cookie loose pack', 'Guangdong province', 'OREO', '376', '10.00', '1000', '77.webp', '78.webp', '0');
INSERT INTO `goods` VALUES ('40', 'biscuits', 'Oreo Wafer biscuit Double heart Crunch bulk multi-flavor mixed snack food individually packaged small snacks for children', 'Beijing', 'OREO', '500', '45.00', '1000', '79.webp', '80.webp', '0');


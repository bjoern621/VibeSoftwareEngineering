/**
 * Utility für Fahrzeugbilder
 * Ordnet Fahrzeugmarken und -modellen passende Bilder zu
 */

// Mapping von Marke/Modell zu Unsplash-Bildern
const vehicleImageMap = {
  // BMW
  'bmw x5': 'https://media.istockphoto.com/id/2081018702/de/foto/bmw-x5-xdrive40i-hybrid-display-bei-einem-h%C3%A4ndler-bmw-bietet-den-x5-xdrive40i-mit-eboost-48v.jpg?s=612x612&w=0&k=20&c=JSfr44Iwp5LmWdCtxJFS2af4SvUQWdeNgzpAjk4nuRE=',
  'bmw 3er': 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=600&fit=crop',
  'bmw 5er': 'https://media.istockphoto.com/id/915658280/de/foto/bmw-5er-reihe-deutsche-bayerischen-automobilhersteller.jpg?s=612x612&w=0&k=20&c=jlb8_OWkRA7-DXCmidQTowiRSUDOI4PvHTp1igPdY8Y=',

  // VW
  'vw golf': 'https://media.istockphoto.com/id/1487370284/de/foto/kleinwagen-volkswagen-golf-auf-einem-parkplatz.jpg?s=612x612&w=0&k=20&c=FdA6M81XLHnyaTFiYM6XJ3P8qFb0csmcaq5S8LvzzxM=',
  'volkswagen golf':
    'https://media.istockphoto.com/id/1487370284/de/foto/kleinwagen-volkswagen-golf-auf-einem-parkplatz.jpg?s=612x612&w=0&k=20&c=FdA6M81XLHnyaTFiYM6XJ3P8qFb0csmcaq5S8LvzzxM=',
  'vw transporter':
    'https://media.istockphoto.com/id/695144852/de/foto/wei%C3%9Fen-volkswagen-transporter.jpg?s=612x612&w=0&k=20&c=3gcgG5YbtJkuWAIc5F8B5zI32Vh8Qjd4Ep-TaQN0pCc=',
  'volkswagen transporter':
    'https://media.istockphoto.com/id/695144852/de/foto/wei%C3%9Fen-volkswagen-transporter.jpg?s=612x612&w=0&k=20&c=3gcgG5YbtJkuWAIc5F8B5zI32Vh8Qjd4Ep-TaQN0pCc=',
  'vw polo': 'https://media.istockphoto.com/id/482016057/de/foto/modell-volkswagen-polo-2014.jpg?s=612x612&w=0&k=20&c=S5Ef4umg3pFOV0RISCBggxDux0QpF_cM1IbVG8m6BjI=',
  'volkswagen polo':
    'https://media.istockphoto.com/id/482016057/de/foto/modell-volkswagen-polo-2014.jpg?s=612x612&w=0&k=20&c=S5Ef4umg3pFOV0RISCBggxDux0QpF_cM1IbVG8m6BjI=',
  'vw passat':
    'https://media.istockphoto.com/id/534048018/de/foto/grau-auto-vw-passat-auf-schotterstrecke.jpg?s=612x612&w=0&k=20&c=abES1INg9tFObPODQGDk3AQMHq68DMMyk1oaHcduZx4=',
  'volkswagen passat':
    'https://media.istockphoto.com/id/534048018/de/foto/grau-auto-vw-passat-auf-schotterstrecke.jpg?s=612x612&w=0&k=20&c=abES1INg9tFObPODQGDk3AQMHq68DMMyk1oaHcduZx4=',
  'vw crafter':
    'https://media.istockphoto.com/id/582274022/de/foto/volkswagen-crafter-van-unterwegs.jpg?s=612x612&w=0&k=20&c=K0qol7TU1K2JDXwdDzZOy906N9vougO1knNyXzmWXQw=',
  'volkswagen crafter':
    'https://media.istockphoto.com/id/582274022/de/foto/volkswagen-crafter-van-unterwegs.jpg?s=612x612&w=0&k=20&c=K0qol7TU1K2JDXwdDzZOy906N9vougO1knNyXzmWXQw=',

  // Mercedes
  'mercedes c-klasse':
    'https://media.istockphoto.com/id/869448732/de/foto/mercedes-benz-c-klasse-auf-ein-auf-l%C3%A4ndliche-stra%C3%9Fe-geparkt.jpg?s=612x612&w=0&k=20&c=SfCKFEWDpbfDoFLPBGdAXRiuFlfiEme-XB9KnqI8n5Q=',
  'mercedes e-klasse':
    'https://media.istockphoto.com/id/1017599130/de/foto/geparkt-am-city-stra%C3%9Fenrand-mercedes-auto-e-klasse.jpg?s=612x612&w=0&k=20&c=-EUHZ7qYb7QusMBXw9yfjiZBBYYKcmNl7dNeZL9bgWw=',
  'mercedes sprinter':
    'https://media.istockphoto.com/id/157733862/de/foto/white-mercedes-benz-transportunternehmen-in-einer-reihe.jpg?s=612x612&w=0&k=20&c=i93ZyipLwArOF-DMOE2kFSlZuaTRggZHrGfzvYgAeBY=',

  // Audi
  'audi q5': 'https://media.istockphoto.com/id/1368795087/de/foto/audi-q5-sportback-testfahrttag.jpg?s=612x612&w=0&k=20&c=fbsCMgiqYfXlxcit-qYkJwvm9i0X-H_l3dcnzCuF8wk=',
  'audi q7': 'https://media.istockphoto.com/id/910265816/de/foto/audi-q7-auf-dem-parkplatz.jpg?s=612x612&w=0&k=20&c=MlwxoCbuwZHEg_ce12i2Q807jdzpoST6ZC3TeBDJCnw=',
  'audi a4': 'https://media.istockphoto.com/id/459011703/de/foto/audi-a4-quattro-2013.jpg?s=612x612&w=0&k=20&c=Bw1Ue-wvzBbV_rqvHWGai9IWyyU367Cm52-yrRkE31A=',
  'audi a6': 'https://media.istockphoto.com/id/496954872/de/foto/audi-6-serie-7.jpg?s=612x612&w=0&k=20&c=AvIrY3Ax9T2p81xUPRiIs-FWNqRJ8736_1bLu4PK4kA=',

  // Fiat
  'fiat 500': 'https://media.istockphoto.com/id/1223162659/de/foto/fiat-500-dolce-10.jpg?s=612x612&w=0&k=20&c=TR7OkoXPEmN6YAP0-uJT1mM23ywvEccpUIdZecWHzR4=',

  // Ford
  'ford kuga': 'https://media.istockphoto.com/id/534046130/de/foto/wei%C3%9Fe-auto-ford-kuga-schnell-fahren-sie-auf-der-stra%C3%9Fe.jpg?s=612x612&w=0&k=20&c=Do8ecNSrU-UZuO5dErfVO6MfH85tfm2NhE3tgqXoYto=',
  'ford transit':
    'https://media.istockphoto.com/id/1341721453/de/foto/ford-transit.jpg?s=612x612&w=0&k=20&c=qyFS1KD4FUpBhJgLwJkdyWPhtm-yp3KX_b27OumRPOk=',

  // Opel
  'opel vivaro':
    'https://media.istockphoto.com/id/2203220954/de/foto/seitenansicht-des-wei%C3%9Fen-opel-vivaro.jpg?s=612x612&w=0&k=20&c=XQ0LYjWvr31e3YDVbpKWNGhTA79w_ONf5JnhuSvpVr4=',
  'opel corsa':
    'https://media.istockphoto.com/id/1348943937/de/foto/opel-corsa.jpg?s=612x612&w=0&k=20&c=GgkPliLeNyD3a3pjn5r1m2wRCF-tyYBpsKh24KnYKNk=',

  // Renault
  'renault clio':
    'https://media.istockphoto.com/id/512145112/de/foto/renault-clio.jpg?s=612x612&w=0&k=20&c=zkZMajSzUru8Q96Atq47PY25KXiWYwxAS9KdI-SzY-U=',

  // Skoda
  'skoda octavia':
    'https://media.istockphoto.com/id/489346234/de/foto/skoda-octavia-kombi-scout-estate-auto.jpg?s=612x612&w=0&k=20&c=UM9vaOpgNC1OeSblPK1GDo-G6ww-N0ObI1GFjBKPA6M=',
  'skoda kodiaq':
    'https://media.istockphoto.com/id/2164234602/de/foto/blauer-skoda-kodiaq-auf-landstra%C3%9Fe-in-den-bergen.jpg?s=612x612&w=0&k=20&c=rYEhqqBCT87OqhRSfQ7rRH0klsX0zRc7x62VC-YQ1WM=',

  // Peugeot
  'peugeot 208':
    'https://media.istockphoto.com/id/2199989863/de/foto/profilansicht-des-grauen-peugeot-208-der-auf-der-stra%C3%9Fe-geparkt-ist.jpg?s=612x612&w=0&k=20&c=O5hp0cllB755IY9SgLF_0KPNPqVdkWjUcfIoDzOiHDM=',

  // Toyota
  'toyota rav4':
    'https://media.istockphoto.com/id/1388853267/de/foto/blauer-toyota-rav4-auf-unbefestigter-stra%C3%9Fe-in-utah-wilderness.jpg?s=612x612&w=0&k=20&c=2sgVYLWXS3hCkCwpvdrPJfoG3q4wr1I6LNiBpZ4lUoM=',
};

// Fallback-Bilder nach Fahrzeugtyp
const fallbackByType = {
  COMPACT_CAR:
    'https://images.unsplash.com/photo-1583267746897-c2342a2da9b0?w=800&h=600&fit=crop',
  SEDAN: 'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?w=800&h=600&fit=crop',
  SUV: 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=600&fit=crop',
  VAN: 'https://images.unsplash.com/photo-1527234707258-c49cb5c81c22?w=800&h=600&fit=crop',
};

// Default-Fallback
const defaultImage =
  'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800&h=600&fit=crop';

/**
 * Ermittelt das passende Bild für ein Fahrzeug
 * @param {string} brand - Fahrzeugmarke
 * @param {string} model - Fahrzeugmodell
 * @param {string} vehicleType - Fahrzeugtyp (COMPACT_CAR, SEDAN, SUV, VAN)
 * @returns {string} Bild-URL
 */
export const getVehicleImage = (brand, model, vehicleType) => {
  if (!brand || !model) {
    return vehicleType ? fallbackByType[vehicleType] || defaultImage : defaultImage;
  }

  // Normalisiere zu lowercase für case-insensitive matching
  const key = `${brand.toLowerCase()} ${model.toLowerCase()}`;

  // Exakte Übereinstimmung
  if (vehicleImageMap[key]) {
    return vehicleImageMap[key];
  }

  // Fallback auf Typ
  if (vehicleType && fallbackByType[vehicleType]) {
    return fallbackByType[vehicleType];
  }

  // Default
  return defaultImage;
};

export default getVehicleImage;
